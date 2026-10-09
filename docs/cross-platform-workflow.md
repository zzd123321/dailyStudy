# macOS：工具安装与学习工作流

在 Mac 上准备 JDK、Maven、Git 和一份仓库，通过 GitHub 保存源码与学习记录。如果使用多台 Mac，各自安装工具并克隆仓库；编译输出在本地重新生成。

## 1. 准备工具

从 [Temurin 下载页](https://adoptium.net/temurin/releases/?version=21) 选择 Java 21、macOS、JDK。Apple Silicon 通常选 aarch64/arm64，Intel Mac 选 x64。`uname -m` 可查看机器架构。

安装官方 PKG 后，在终端检查：

```bash
/usr/libexec/java_home -V
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
java -version
javac -version
```

需要每次打开终端自动生效时，在自己使用的 shell 配置中添加上述 JAVA_HOME/PATH 设置，不要覆盖原有配置。macOS 默认 zsh 通常使用 `~/.zshrc`。

Maven 按 [官方安装说明](https://maven.apache.org/install.html) 下载 Binary tar.gz，核对官方 SHA-512 后解压到固定目录，把实际 `bin` 目录加入 PATH。可用 `shasum -a 512 压缩包路径` 查看散列值。

如果已经使用 Homebrew，也可通过它安装 Maven：

```bash
brew install maven
```

无论哪种安装方式，最后检查：

```bash
mvn -version
git --version
command -v java
command -v javac
```

如果 `git --version` 提示需要安装开发者工具，可运行 `xcode-select --install` 并完成系统安装提示，然后重新打开终端。也可使用已有的 Homebrew 安装 Git：`brew install git`，选择一种方式即可。

如果 Homebrew 或其他工具安装了额外 JDK，确认当前 JAVA_HOME 和 Maven 仍使用 Java 21。多台 Mac 只需主版本一致，不要求目录名或补丁版本完全相同。

VS Code 可安装 [Extension Pack for Java](https://code.visualstudio.com/docs/java/java-tutorial)。用 Java 扩展的运行环境设置确认项目 JDK，命令行检查也需要通过。

以上是 macOS 本地操作说明，当前课程代码在 Linux 云环境实测；没有在你的 Mac 上执行安装。

## 2. 首次克隆仓库

在适合放项目的目录执行；如果使用多台 Mac，每台首次克隆一次：

```shell
git clone https://github.com/zzd123321/dailyStudy.git
cd dailyStudy
git status --short --branch
```

不要把 `target/`、`out/`、本地 JDK 或 Maven 安装包提交到仓库。Git 负责源文件，构建工具负责生成输出。

Git 的提交者信息与 GitHub 登录是不同的事情。需要时设置仓库级提交信息，下面内容替换为自己的：

```shell
git config user.name "你的名字"
git config user.email "you@example.com"
```

邮箱可以使用 GitHub 提供给自己的 noreply 地址；不要照抄示例当作真实身份。

## 3. 每次开始学习

先查看状态：

```shell
git status --short --branch
```

工作区没有未提交改动时：

```shell
git pull --ff-only
```

有未提交改动时，先查看差异并保存自己的工作。不要通过删除文件或强制 reset 来“解决”状态。

开始学习前检查当前分支为 main，并已拿到远端最新提交。

## 4. 每次结束学习

从仓库根目录检查和提交自己确认的文件：

```shell
git status --short
git diff
git add notes/day-001.md
git add projects/java-foundations/src/main/java/com/dailystudy/day001/LearningBudget.java
git commit -m "study: complete day 001 exercises"
git push origin main
```

后续课程替换为当天实际修改的文件。没有改动就不必制造空提交。源码若只是练习后恢复原样，也不需要强行添加改动。

简单规则：**学习结束后推送，下一次开始前拉取。** 多台 Mac 也使用同样的流程。

## 5. pull 或 push 被拒绝怎么办

`pull --ff-only` 失败或 push 显示分支落后时，先停止继续修改，查看：

```shell
git status
git log --oneline --graph --all -8
```

若自己的改动还未提交，先保存并提交。然后在当前分支执行 `git pull --rebase`，把本地提交接到远端之后。出现冲突时，打开冲突文件，保留需要的内容，移除冲突标记；对确认的文件执行 `git add 文件路径`，再 `git rebase --continue`。

若不理解冲突，使用 `git rebase --abort` 回到此次 rebase 前的状态，并记录日志求助。不要使用强制推送覆盖另一台电脑的提交。

Git 身份验证失败与分支落后不同。前者通过本地 Git 客户端正常登录处理，后者处理提交历史；不要在源码或笔记里写登录凭据。

## 6. 编码与换行

仓库 `.gitattributes` 统一文本换行。编辑器文件编码使用 UTF-8，编译命令明确使用 UTF-8。

Java 21 默认字符集通常为 UTF-8，但终端显示还受终端配置影响。中文乱码时先确认源码和终端编码为 UTF-8，并使用支持中文的字体。不要把乱码输出当作业务代码错误直接改字符串。

文件和类名的大小写始终保持一致。有些本地文件系统对大小写不敏感，Linux 云环境可能会暴露错误。

## 7. 路径与运行目录

本课 Java/Maven 命令在 macOS 终端执行：

```shell
javac -encoding UTF-8 -d out examples/HelloStudy.java
java -cp out HelloStudy
mvn -B -ntp compile
java -cp target/classes com.dailystudy.day001.LearningBudget
```

前两条从 `lessons/day-001` 执行，后两条从 `projects/java-foundations` 执行。不要在同一个目录连续照抄四条。

今天 classpath 只有一个目录，无需分隔符。以后在 macOS 上有多个条目时，使用 `:` 分隔。

课程中的 `/workspace/...` 路径和工具激活脚本用于 Linux 云环境。macOS 本地使用自己的仓库路径，并按本页设置 JAVA_HOME 与 PATH。
