# Day 001：搭好 Java 开发环境，运行你的第一个程序

今天约 2.5 小时。你会写一个学习时长计算器，并能够解释它是如何从代码变成输出的。

你已有前端经验，变量、函数、文件夹和 Git 应该比较熟悉。今天把这些经验接到 Java：明确类型、编译步骤、程序入口、工具链和运行目录。

## 1. 今天的目标与时间安排

| 用时 | 内容 | 可以检查的结果 |
|---:|---|---|
| 10 分钟 | 阅读目标，克隆仓库 | 知道今天从哪个目录开始 |
| 35 分钟 | 安装/检查 JDK、Maven、Git | 三项版本检查正常 |
| 20 分钟 | 理解源码、字节码、JVM | 能说明 java 与 javac 的区别 |
| 25 分钟 | 手动编译 HelloStudy | 命令行打印中文问候 |
| 25 分钟 | 阅读并运行学习时长计算器 | 得到每周 10 小时、48 周 480 小时 |
| 15 分钟 | 用 Maven 构建 | 理解 compile 与运行程序的区别 |
| 15 分钟 | 完成练习、解释错误 | 能修改参数并说明结果 |
| 5 分钟 | 记录与 Git 同步 | 下一台电脑可以接着学 |

如果安装耗时超过预计，允许扩展到三小时。今天至少完成工具检查、手动编译运行和一次独立修改；不要只安装软件就把今天标为学会。

## 2. 安装前，先认识三个东西

**JVM** 是执行 Java 字节码的虚拟机实现。你安装的 Java 工具会提供相应平台的 JVM。

**运行时** 提供运行 Java 程序需要的能力。只有运行能力，未必有编译器。

**JDK** 是开发工具包，包含运行能力和 `javac` 等开发工具。今天需要的是 Java 21 JDK。

两个最重要的命令：

```shell
java -version
javac -version
```

`java` 用于启动运行环境、运行程序；`javac` 用于把 `.java` 源文件编译成 `.class` 字节码。只看到第一条成功，不能说明开发环境已经齐备。

我们还会使用 Maven。它管理项目构建和依赖，不是 JVM，也不是另一门语言。

## 3. macOS 安装

按 [macOS 操作说明](../../docs/cross-platform-workflow.md#1-准备工具) 操作。

工具要求：

- 安装 Java 21 **JDK**，选择与你电脑架构匹配的版本。
- Apple Silicon 的 Mac 是 arm64，Intel Mac 是 x64。以自己设备为准。
- 检查 `java` 和 `javac` 的主版本为 21。
- 安装 Maven 3.9.x 和 Git。
- Maven 的版本输出中也应显示它正在使用 Java 21。
- 安装后重新打开终端；编辑器里的终端也需要重新启动才能读取新的环境变量。

编辑器先使用你熟悉的 VS Code，安装 Extension Pack for Java 即可。今天用命令行验证，不把 IDE 按钮当作工具链是否正常的唯一依据。

云环境用户在 `/workspace/dailyStudy` 执行：

```bash
bash scripts/setup-cloud-java.sh
source /workspace/.dailystudy-tools/env.sh
```

这是云环境 Linux x64 的专用脚本。macOS 本地按上面的安装说明操作。

## 4. 克隆项目并理解当前目录

本地首次使用，从你打算存放项目的目录执行：

```shell
git clone https://github.com/zzd123321/dailyStudy.git
cd dailyStudy
git status --short --branch
```

已经有这个仓库就不要重复克隆，按 Git 同步工作流拉取即可。当前云环境已有仓库，也不需要重复克隆或创建 worktree。

`cd` 会改变当前目录，后面的相对路径都从这个目录开始计算。

检查目录的方法：

```bash
# macOS 终端
pwd
ls
```

接下来的每组命令都会说明起点。遇到找不到文件，先检查目录，再检查命令。

## 5. 理解 Java 的执行过程

```mermaid
flowchart LR
    A[HelloStudy.java 源码] -->|javac 编译| B[HelloStudy.class 字节码]
    B -->|java 启动 JVM| C[执行程序并输出结果]
```

以你熟悉的前端为参考：TypeScript 通常先变成 JavaScript，再由浏览器或 Node.js 执行。Java 的普通编译过程先得到字节码，再由 JVM 执行。具体构建链路不同，但都需要区分“代码文件”“转换步骤”和“执行环境”。

今天记住三件事：

1. `.java` 是你编辑的源代码。
2. `.class` 是 `javac` 生成的字节码，通常不手工修改，也不提交到 Git。
3. `java` 命令加载类并执行入口。运行时如何解释执行或即时编译，以后再深入。

修改 `.java` 后，如果仍运行旧 `.class`，看到的可能还是旧结果。今天就会用练习体验这一点。

## 6. 手动编译并运行 HelloStudy

**从仓库根目录开始。**

```shell
cd lessons/day-001
```

打开 `examples/HelloStudy.java`：

```java
public class HelloStudy {
    public static void main(String[] args) {
        System.out.println("你好，Java！");
    }
}
```

逐项解释：

| 内容 | 今天需要理解的意思 |
|---|---|
| `public class HelloStudy` | 声明公开类，名字是 HelloStudy；该文件应叫 HelloStudy.java |
| `{ ... }` | 划定类或方法的代码范围 |
| `main` | 这个程序使用的入口方法 |
| `public` | 可被外部访问 |
| `static` | 这个入口无需先创建 HelloStudy 对象就可调用 |
| `void` | 方法不返回结果值；打印与返回是两回事 |
| `String[] args` | 命令行传入的字符串数组，今天暂不使用 |
| `System.out.println(...)` | 向标准输出打印，并换行 |
| `;` | 结束这条语句 |

现在执行：

```shell
javac -encoding UTF-8 -d out examples/HelloStudy.java
java -cp out HelloStudy
```

预期输出：

```text
你好，Java！
```

命令参数解释：

- `-encoding UTF-8`：告诉编译器源文件使用 UTF-8 编码。
- `-d out`：把编译产物放进 `out` 目录；`javac` 会创建需要的输出目录。
- `-cp out`：运行时从 `out` 目录查找类；`cp` 是 classpath 的缩写。
- `HelloStudy`：这里写类名，不带 `.java` 或 `.class` 扩展名。

此时 `out/HelloStudy.class` 应存在。`out/` 已加入 Git 忽略规则。

**动手验证：**把问候改成自己的名字，重新执行两条命令。若只改代码而不重新编译，这组运行命令仍加载原有 `.class`。

## 7. 用 Java 计算你的学习预算

上一步现在位于 `lessons/day-001`。返回仓库根目录，再进入项目：

```shell
cd ../..
cd projects/java-foundations
```

打开 `src/main/java/com/dailystudy/day001/LearningBudget.java`：

```java
package com.dailystudy.day001;

public class LearningBudget {
    public static void main(String[] args) {
        String learner = "AI 全栈学习者";
        int studyDaysPerWeek = 4;
        double studyHoursPerDay = 2.5;
        int totalWeeks = 48;

        double weeklyHours = studyDaysPerWeek * studyHoursPerDay;
        double plannedHours = weeklyHours * totalWeeks;

        System.out.println("学习者：" + learner);
        System.out.println("每周学习天数：" + studyDaysPerWeek);
        System.out.println("每天学习小时：" + studyHoursPerDay);
        System.out.println("每周学习小时：" + weeklyHours);
        System.out.println(totalWeeks + "周计划学习小时：" + plannedHours);
    }
}
```

`package` 提供类的命名空间。这个类的完整名称是 `com.dailystudy.day001.LearningBudget`。目录结构与包名对应，有利于项目组织。

`String` 表示字符串，`int` 表示整数，`double` 表示浮点数。它们写在变量名前面：

```java
int studyDaysPerWeek = 4;
double studyHoursPerDay = 2.5;
```

你可以把它们与 TypeScript 的 `const studyDaysPerWeek: number = 4` 对照。Java 区分整数与浮点类型，所以 `int hours = 2.5;` 不合法。`double` 也有浮点精度限制；今天的时间示例适合使用它，业务金额后续单独学习。

两条计算语句对应：

```text
每周学习小时 = 每周学习天数 × 每天学习小时
总计划学习小时 = 每周学习小时 × 总周数
```

字符串参与 `+` 时会拼接文本。这里的数字先通过单独语句计算，再打印，便于阅读和修改。

## 8. 用 Maven 构建，然后运行

**当前目录是 `projects/java-foundations`，这里有 `pom.xml`。**

```shell
mvn -B -ntp compile
java -cp target/classes com.dailystudy.day001.LearningBudget
```

第一次 Maven 构建需要从 Maven Central 下载构建插件，可能比后续构建慢。下载失败时保留报错，检查网络和证书，不要通过关闭验证来绕过。

预期程序输出：

```text
学习者：AI 全栈学习者
每周学习天数：4
每天学习小时：2.5
每周学习小时：10.0
48周计划学习小时：480.0
```

Maven 在这里承担什么：

| 前端中熟悉的概念 | 当前 Java 项目的对应概念 |
|---|---|
| package.json 的部分职责 | pom.xml：项目标识、依赖与构建配置 |
| 依赖下载源 | Maven Central 等仓库 |
| 构建工具输出目录 | target/ |
| 编译步骤 | `mvn compile` 执行到编译阶段 |

这只是帮助理解的对应，不意味着两套工具完全相同。

`pom.xml` 里今天重点看：

- `groupId/artifactId/version`：项目坐标。
- `maven.compiler.release`：编译目标 Java 版本，这里是 21。
- `project.build.sourceEncoding`：源码编码。
- `maven-compiler-plugin`：参与编译的构建插件。

`mvn compile` 会编译项目，**不会执行这个 main 方法**。因此后面仍然使用 `java` 运行。`target/classes` 是 Maven 的类输出目录，完整类名包含 package。

今天还没有 JUnit 测试套件，不把一个零测试的 `mvn test` 当作已验证功能。当前验收使用实际编译与运行结果；后续再建立业务测试。

## 9. 先预测，再运行练习

打开 [练习题](exercises.md)，完成 A–D；E/F 作为有余力时的扩展。

每题执行同样步骤：

1. 不运行，先写预期。
2. 修改源代码。
3. 编译并运行。
4. 比较预期和实际，解释差异。

参考答案在 [solutions.md](solutions.md)，先做再查看。

## 10. 常见错误如何排查

| 现象 | 首先检查 | 处理方向 |
|---|---|---|
| `javac` 找不到，`java` 正常 | 是否只装运行时，PATH 是否指向 JDK | 安装 JDK，重新打开终端 |
| Maven 使用其他 Java 版本 | `mvn -version`、JAVA_HOME | 让 Java、javac、Maven 指向同一套 JDK 21 |
| `file not found` | 当前目录、源文件相对路径 | 对照本课每组命令的起点 |
| `class ... should be declared in a file named ...` | 公开类名与文件名 | 保持名称与大小写一致 |
| `Could not find or load main class` | classpath、包名、是否编译 | 使用正确输出目录和完整类名 |
| 修改后输出仍是旧的 | 是否重新编译 | 重新执行 javac 或 Maven compile |
| 中文乱码 | 源文件是否 UTF-8、终端编码与字体 | 按 macOS 文档调整显示，不把中文改成乱码字符串 |
| Maven 提示缺少 POM | 当前目录里有无 pom.xml | 进入 projects/java-foundations |

一次只改变一个因素。不要遇到错误就同时重装 JDK、IDE 和 Maven。

## 11. 今天的闭卷验收

- [ ] `java -version` 和 `javac -version` 都显示 Java 21。
- [ ] `mvn -version` 显示使用 Java 21。
- [ ] 我能手动编译并运行 HelloStudy。
- [ ] 我能运行 Maven 项目并得到 10.0 与 480.0。
- [ ] 我能修改每天学习时长，预测结果并验证。
- [ ] 我能说明源码、字节码、JVM 的关系。
- [ ] 我能说明编译与运行为什么是两步。
- [ ] 我知道运行命令所在目录和 classpath 的作用。

如果计算器运行了，但你无法解释变量、计算或重新编译，就再做一次独立修改。安装成功与掌握内容分别记录。

## 12. 学习记录与 Git 同步

从项目目录返回仓库根目录：

```shell
cd ../..
```

按 [记录模板](../../docs/learning-log-template.md) 创建 `notes/day-001.md`，写实际结果。然后：

```shell
git status --short
git add notes/day-001.md
git add projects/java-foundations/src/main/java/com/dailystudy/day001/LearningBudget.java
git commit -m "study: complete day 001 exercises"
git push origin main
```

只提交自己理解和确认的改动；`git status` 里若有其他文件，先查看差异。如果换另一台 Mac 学习，开始前在干净工作区使用 `git pull --ff-only`。详细冲突处理见 [macOS 学习工作流](../../docs/cross-platform-workflow.md)。

## 13. 今天只需要这些资料

1. [Temurin JDK 21 下载](https://adoptium.net/temurin/releases/?version=21)：选操作系统和架构；不要选只有运行能力的包。
2. [Java 官方 Getting Started](https://dev.java/learn/getting-started/)：配合源码、编译、运行的讲解阅读。
3. [Java 21 javac 命令说明](https://docs.oracle.com/en/java/javase/21/docs/specs/man/javac.html)：今天只查 encoding、d。
4. [Apache Maven 安装说明](https://maven.apache.org/install.html)：安装和检查；不需要现在学完整生命周期。
5. [VS Code Java 入门](https://code.visualstudio.com/docs/java/java-tutorial)：已有编辑器经验时快速设置。
6. [B 站视频检索：Java 21 JDK 安装](https://search.bilibili.com/all?keyword=Java%2021%20JDK%20%E5%AE%89%E8%A3%85)：这是检索入口，未指定或背书单个视频；只补看安装或 Hello World 对应小节。

学习先读本课，只有卡住或想核对细节时再打开资料。无需看完全部入口才动手。

## 14. 下次从这里接上

[Day 002](../day-002/README.md) 进入 HTTP 请求与响应：浏览器网络面板、curl、请求头、JSON 正文、状态码。你今天已经能运行后端语言的小程序，下次会认识未来后端接口与前端交互的契约。第 3 周再系统展开 Java 类型、控制流与方法。

下一次继续教学时，带上验收结果和不理解的地方；若有报错，附命令、当前目录和错误原文。只读完文章不作为通过条件。
