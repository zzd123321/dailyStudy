---
description: 在 macOS 准备 Java 21、Maven、Git 与静态知识网站。
---

# macOS 开发环境

读网站不需要安装 Java。运行 Java 示例时准备 JDK、Maven 和 Git；在自己的 Mac 运行或修改知识网站，再准备 Node.js。

## 安装 Java 21 JDK

从 [Temurin 下载页](https://adoptium.net/temurin/releases/?version=21) 选择 Java 21、macOS、**JDK**。Apple Silicon 通常选 aarch64/arm64，Intel Mac 选 x64。用 `uname -m` 查看机器架构。

安装官方 PKG 后检查：

```bash
/usr/libexec/java_home -V
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
java -version
javac -version
```

两个命令都应显示 Java 21。需要每次新终端自动生效时，把 JAVA_HOME 与 PATH 两行加入自己的 shell 配置；macOS 默认 zsh 通常使用 `~/.zshrc`，添加时保留原有内容。

只安装运行时可能没有 javac，要分别检查运行器与编译器。IDE 与终端也可能使用不同 JDK，两个地方都应指向同一主版本。

## 安装 Maven 与 Git

已有 Homebrew 时：

```bash
brew install maven
mvn -version
```

也可按 [Maven 官方安装说明](https://maven.apache.org/install.html) 下载 3.9.x 的 Binary tar.gz，核对 SHA-512，解压到固定目录并把 bin 加入 PATH。

Maven 的输出应显示使用 Java 21。Homebrew 可能引入另一个 JDK，因此仍需检查 JAVA_HOME 与实际工具版本。

```bash
git --version
command -v java
command -v javac
command -v mvn
```

Git 未安装时，可运行 `xcode-select --install` 并按系统提示安装。编辑器可用 VS Code，加装 [Extension Pack for Java](https://code.visualstudio.com/docs/java/java-tutorial)。

## 获取代码

首次获取：

```bash
git clone https://github.com/zzd123321/dailyStudy.git
cd dailyStudy
```

已有仓库时先保存本地改动，工作区干净后更新：

```bash
git pull --ff-only origin main
```

多台 Mac 各自安装工具并克隆。同步的是源码，target/、out/、node_modules/ 等产物在各机器重新生成。

## 本地阅读知识网站

安装 [Node.js](https://nodejs.org/en/download) 的受支持 LTS 版本，要求 Node.js 22 或更高。从仓库根目录执行：

```bash
node --version
npm ci
npm run dev
```

打开终端显示的地址，通常为 `http://localhost:5173/dailyStudy/`。按 Ctrl+C 停止开发服务器。

查看生产构建：

```bash
npm run build
npm run preview
```

预览通常为 `http://localhost:4173/dailyStudy/`，以终端输出为准。网站构建产生静态 HTML、CSS 和 JavaScript。

## 网站与课程程序各自运行

| 内容 | 所需工具 | 运行方式 |
|---|---|---|
| 知识网站 | Node.js、npm | 仓库根目录 npm run dev |
| Java 最小示例 | JDK 21 | 对应目录 javac、java |
| Maven 项目 | JDK 21、Maven | 有 pom.xml 的目录执行 Maven |
| HTTP 实验页与 API | 运行中的 HTTP 实验程序 | 浏览器打开 http://127.0.0.1:8082 |

静态网站不会启动 HTTP 实验 API。第 02 课的实验页面由 Java 服务提供，两个进程承担不同职责。

## 环境问题先检查这些

- 当前目录：用 pwd 确认；相对路径从这里开始计算。
- 工具版本：Java、javac、Maven 是否一致，Node 是否满足要求。
- 文件名：公开类名与 Java 文件名保持一致，包括大小写。
- 产物：修改源码后重新编译，旧 class 可能仍能运行。
- 中文：源码与终端使用 UTF-8，字体支持中文。
- Git：push 被拒绝时先查看历史，不用强制推送覆盖另一台电脑的改动。

云环境使用 Linux 专用的 scripts/setup-cloud-java.sh；不要在 Mac 上照抄 /workspace 路径或执行该安装脚本。
