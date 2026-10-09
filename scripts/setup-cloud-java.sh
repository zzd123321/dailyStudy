#!/usr/bin/env bash
# Cloud onboarding helper for Linux x86_64; local macOS setup is documented separately.
set -euo pipefail

if [[ "$(uname -s)" != Linux || "$(uname -m)" != x86_64 ]]; then
  echo '此脚本只用于 Linux x86_64 云环境。其他系统请按 Day 001 的 JDK 安装说明操作。' >&2
  exit 1
fi

tools_root=/workspace/.dailystudy-tools
jdk_dir="$tools_root/jdk-21.0.12.1+1"
maven_dir="$tools_root/apache-maven-3.9.11"
jdk_archive=OpenJDK21U-jdk_x64_linux_hotspot_21.0.12.1_1.tar.gz
jdk_url="https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.12.1%2B1/$jdk_archive"
maven_archive=apache-maven-3.9.11-bin.tar.gz
maven_url="https://archive.apache.org/dist/maven/maven-3/3.9.11/binaries/$maven_archive"

mkdir -p "$tools_root"
stage_dir=$(mktemp -d "$tools_root/.setup.XXXXXX")
trap 'rm -rf "$stage_dir"' EXIT

download() {
  curl --fail --location --silent --show-error --retry 2 \
    --connect-timeout 15 --max-time 300 "$1" -o "$2"
}

if [[ ! -d "$jdk_dir" ]]; then
  echo '下载并验证 Temurin JDK 21...'
  download "$jdk_url" "$stage_dir/$jdk_archive"
  download "$jdk_url.sha256.txt" "$stage_dir/jdk.sha256"
  read -r jdk_checksum _ < "$stage_dir/jdk.sha256"
  [[ "$jdk_checksum" =~ ^[0-9a-fA-F]{64}$ ]]
  (cd "$stage_dir"; printf '%s  %s\n' "$jdk_checksum" "$jdk_archive" | sha256sum -c -)
  mkdir "$stage_dir/jdk"
  tar -xzf "$stage_dir/$jdk_archive" -C "$stage_dir/jdk" --strip-components=1
  "$stage_dir/jdk/bin/javac" -version
  mv "$stage_dir/jdk" "$jdk_dir"
fi

if [[ ! -d "$maven_dir" ]]; then
  echo '下载并验证 Apache Maven...'
  download "$maven_url" "$stage_dir/$maven_archive"
  download "$maven_url.sha512" "$stage_dir/maven.sha512"
  read -r maven_checksum _ < "$stage_dir/maven.sha512" || [[ -n "${maven_checksum:-}" ]]
  [[ "$maven_checksum" =~ ^[0-9a-fA-F]{128}$ ]]
  (cd "$stage_dir"; printf '%s  %s\n' "$maven_checksum" "$maven_archive" | sha512sum -c -)
  mkdir "$stage_dir/maven"
  tar -xzf "$stage_dir/$maven_archive" -C "$stage_dir/maven" --strip-components=1
  mv "$stage_dir/maven" "$maven_dir"
fi

"$jdk_dir/bin/javac" -version
mkdir -p "$tools_root/m2"
python3 - <<'PY'
import os
from pathlib import Path
from urllib.parse import urlparse
import xml.etree.ElementTree as ET

root = ET.Element('settings')
ET.SubElement(root, 'localRepository').text = '/workspace/.dailystudy-tools/m2'
proxy_url = os.environ.get('HTTPS_PROXY') or os.environ.get('HTTP_PROXY')
if proxy_url:
    parsed = urlparse(proxy_url)
    if parsed.username or parsed.password:
        raise SystemExit('现有代理包含认证字段；不会把这些字段复制到设置文件。')
    if parsed.scheme != 'http' or not parsed.hostname:
        raise SystemExit('当前代理格式不在此云环境脚本的支持范围。')
    proxies = ET.SubElement(root, 'proxies')
    for protocol in ('http', 'https'):
        proxy = ET.SubElement(proxies, 'proxy')
        for key, value in {
            'id': 'cloud-' + protocol,
            'active': 'true',
            'protocol': protocol,
            'host': parsed.hostname,
            'port': str(parsed.port or 80),
            'nonProxyHosts': 'localhost|127.0.0.1',
        }.items():
            ET.SubElement(proxy, key).text = value
ET.indent(root)
path = Path('/workspace/.dailystudy-tools/maven-settings.xml')
ET.ElementTree(root).write(path, encoding='utf-8', xml_declaration=True)
path.chmod(0o600)
print('已生成本机 Maven 设置；使用现有无认证字段的代理，不输出代理地址。')
PY
cat > "$tools_root/env.sh" <<'ENV'
export JAVA_HOME='/workspace/.dailystudy-tools/jdk-21.0.12.1+1'
export MAVEN_HOME='/workspace/.dailystudy-tools/apache-maven-3.9.11'
export PATH="$JAVA_HOME/bin:$MAVEN_HOME/bin:$PATH"
case " ${MAVEN_ARGS:-} " in
  *' --settings /workspace/.dailystudy-tools/maven-settings.xml '*) ;;
  *) export MAVEN_ARGS="${MAVEN_ARGS:-} --settings /workspace/.dailystudy-tools/maven-settings.xml" ;;
esac
if [[ -r /etc/ssl/certs/java/cacerts ]]; then
  case " ${MAVEN_OPTS:-} " in
    *' -Djavax.net.ssl.trustStore=/etc/ssl/certs/java/cacerts '*) ;;
    *) export MAVEN_OPTS="${MAVEN_OPTS:-} -Djavax.net.ssl.trustStore=/etc/ssl/certs/java/cacerts" ;;
  esac
fi
ENV

export JAVA_HOME="$jdk_dir"
"$maven_dir/bin/mvn" -version
echo '工具链已准备。使用前执行：source /workspace/.dailystudy-tools/env.sh'
