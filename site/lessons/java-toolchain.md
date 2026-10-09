---
description: 理解 JDK、JVM、源码、字节码、classpath 和 Maven，用 Java 编译运行你的第一个程序。
---

# 01 · Java 工具链与程序执行

你熟悉前端的变量、函数和构建工具。本课把这些经验接到 Java：理解源代码如何变成程序，为什么修改后需要重新编译，以及 Maven 在其中负责什么。

阅读后，你应能独立编译、运行并修改一个 Java 程序，遇到错误时判断它发生在编译阶段还是运行阶段。

## 安装前，先认识三个东西

**JVM** 是执行 Java 字节码的虚拟机实现。你安装的 Java 工具会提供相应平台的 JVM。

**运行时** 提供运行 Java 程序需要的能力。只有运行能力，未必有编译器。

**JDK** 是开发工具包，包含运行能力和 `javac` 等开发工具。本课需要的是 Java 21 JDK。

两个最重要的命令：

```shell
java -version
javac -version
```

`java` 用于启动运行环境、运行程序；`javac` 用于把 `.java` 源文件编译成 `.class` 字节码。只看到第一条成功，不能说明开发环境已经齐备。

我们还会使用 Maven。它管理项目构建和依赖，不是 JVM，也不是另一门语言。


## macOS 安装

按 [macOS 开发环境](/setup) 操作。

工具要求：

- 安装 Java 21 **JDK**，选择与你电脑架构匹配的版本。
- Apple Silicon 的 Mac 是 arm64，Intel Mac 是 x64。以自己设备为准。
- 检查 `java` 和 `javac` 的主版本为 21。
- 安装 Maven 3.9.x 和 Git。
- Maven 的版本输出中也应显示它正在使用 Java 21。
- 安装后重新打开终端；编辑器里的终端也需要重新启动才能读取新的环境变量。

编辑器先使用你熟悉的 VS Code，安装 Extension Pack for Java 即可。本课用命令行验证，不把 IDE 按钮当作工具链是否正常的唯一依据。



## 克隆项目并理解当前目录

本地首次使用，从你打算存放项目的目录执行：

```shell
git clone https://github.com/zzd123321/dailyStudy.git
cd dailyStudy
git status --short --branch
```

已经有这个仓库就不要重复克隆，在保存本地改动后执行 `git pull --ff-only` 即可。

`cd` 会改变当前目录，后面的相对路径都从这个目录开始计算。

检查目录的方法：

```bash
# macOS 终端
pwd
ls
```

接下来的每组命令都会说明起点。遇到找不到文件，先检查目录，再检查命令。


## 理解 Java 的执行过程

```text
.java 源码 → javac 编译 → .class 字节码 → JVM 加载和执行 → 程序结果
```

以你熟悉的前端为参考：TypeScript 通常先变成 JavaScript，再由浏览器或 Node.js 执行。Java 的普通编译过程先得到字节码，再由 JVM 执行。具体构建链路不同，但都需要区分“代码文件”“转换步骤”和“执行环境”。

本课记住三件事：

1. `.java` 是你编辑的源代码。
2. `.class` 是 `javac` 生成的字节码，通常不手工修改，也不提交到 Git。
3. `java` 命令加载类并执行入口。运行时如何解释执行或即时编译，以后再深入。

修改 `.java` 后，如果仍运行旧 `.class`，看到的可能还是旧结果。本课就会用练习体验这一点。


## 手动编译并运行 HelloStudy

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

| 内容 | 含义 |
|---|---|
| `public class HelloStudy` | 声明公开类，名字是 HelloStudy；该文件应叫 HelloStudy.java |
| `{ ... }` | 划定类或方法的代码范围 |
| `main` | 这个程序使用的入口方法 |
| `public` | 可被外部访问 |
| `static` | 这个入口无需先创建 HelloStudy 对象就可调用 |
| `void` | 方法不返回结果值；打印与返回是两回事 |
| `String[] args` | 命令行传入的字符串数组，本课暂不使用 |
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


## 用 Java 计算你的学习预算

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

你可以把它们与 TypeScript 的 `const studyDaysPerWeek: number = 4` 对照。Java 区分整数与浮点类型，所以 `int hours = 2.5;` 不合法。`double` 也有浮点精度限制；本课的时间示例适合使用它，业务金额后续单独学习。

两条计算语句对应：

```text
每周学习小时 = 每周学习天数 × 每天学习小时
总计划学习小时 = 每周学习小时 × 总周数
```

字符串参与 `+` 时会拼接文本。这里的数字先通过单独语句计算，再打印，便于阅读和修改。


## 用 Maven 构建，然后运行

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

`pom.xml` 里重点看：

- `groupId/artifactId/version`：项目坐标。
- `maven.compiler.release`：编译目标 Java 版本，这里是 21。
- `project.build.sourceEncoding`：源码编码。
- `maven-compiler-plugin`：参与编译的构建插件。

`mvn compile` 会编译项目，**不会执行这个 main 方法**。因此后面仍然使用 `java` 运行。`target/classes` 是 Maven 的类输出目录，完整类名包含 package。

本课重点是实际编译和运行。仓库已经加入第 03 课的 JUnit 测试，但 `mvn compile` 本身不执行测试，也不能证明所有功能正确。


## 编译错误与运行错误：发生在不同阶段

一个程序至少经过三个检查点：源文件能否被找到、源码能否编译、运行时能否加载并执行。看到错误时，先判断卡在哪一个检查点，再选择工具。

```java
int hours = 2.5;
```

这一行在编译阶段失败。`int` 不能直接保存小数，编译器拒绝产生这份新代码的字节码。即使目录里还有旧 `.class`，它也不能证明新源码正确。

```java
int count = 0;
System.out.println(10 / count);
```

这个例子能编译，但执行时会因整数除以零抛出 `ArithmeticException`。类型正确不代表所有运行条件都正确；后端接收用户输入后，更需要检查数值是否符合规则。

| 错误发生点 | 常见现象 | 先检查什么 |
|---|---|---|
| 找不到源文件 | `file not found` | `pwd`、相对路径、文件名大小写 |
| 编译源码 | 类型不兼容、分号缺失 | 第一条相关编译错误与对应源码行 |
| 加载入口类 | `Could not find or load main class` | 是否编译、classpath、完整类名 |
| 执行程序 | 异常与调用栈 | 异常类型、自己的源码位置、触发输入 |
| 得到错误结果 | 可以运行但数字不对 | 公式、参数、运算顺序、是否加载旧字节码 |

不要只盯住最后一行报错。Java 调用栈通常包含异常名称与 `文件名:行号`，优先找自己项目的第一处相关位置。

## 包名、文件路径与 classpath 的关系

源码声明：

```java
package com.dailystudy.day001;
```

编译后，文件通常位于：

```text
target/classes/
└── com/dailystudy/day001/LearningBudget.class
```

启动 JVM 时分清两个参数：

```bash
java -cp target/classes com.dailystudy.day001.LearningBudget
```

`target/classes` 是查找的根目录；完整类名说明从根目录下找哪个类。不是把 `-cp` 指向包目录后再省略包名。JVM 根据完整名称与 classpath 查找类，命名空间也用于区分不同包中的同名类。

如果运行时还需要外部库，classpath 要包含这些库。后面的 HTTP 实验使用：

```bash
java -cp 'target/classes:target/dependency/*' com.dailystudy.day002.HttpPlayground
```

macOS 的多个条目用 `:` 分隔。引号让 shell 不提前展开 `*`，JVM 使用目录里的 JAR；这不等于 shell 把所有 Java 文件编译了。

## Maven 生命周期：命令执行到哪一步

常用命令不是互不相关的按钮。Maven 默认生命周期里，后面的阶段会执行前面的相关阶段：

```text
validate → compile → test → package → verify → install → deploy
```

| 命令 | 主要作用 | 本项目中是否运行 main |
|---|---|---|
| `mvn compile` | 编译主源码 | 否 |
| `mvn test` | 编译主源码与测试源码，执行测试 | 不会自动启动业务 main |
| `mvn package` | 测试并打包 | 否 |
| `mvn clean` | 删除本项目构建产物 | 否，是另一个生命周期 |

`mvn clean test` 会先清理再执行测试，可减少旧产物干扰。日常不必每次都 clean。`mvn install` 把产物装入本机 Maven 仓库，`mvn deploy` 发布 Maven 产物到远程仓库；它们不等同于把业务网站部署到服务器。

`-B` 使用非交互模式；`-ntp` 隐藏依赖下载进度。它们让输出更简洁，不改变业务计算。

### 为什么需要区分插件与依赖

依赖是你的代码使用的库，例如 HTTP 实验中的 Jackson。插件参与构建步骤，例如编译插件和运行测试的 Surefire。它们都可能由 Maven 下载，但职责不同。

`pom.xml` 里的 Java 21 编译配置约束语言/API 目标；实际执行 Maven 的 JDK 仍由本机环境决定。所以要同时看 `javac -version` 与 `mvn -version`，不能只检查配置文件。

## 运算顺序与字符串拼接

```java
System.out.println("总数：" + 4 + 2);    // 总数：42
System.out.println("总数：" + (4 + 2));  // 总数：6
System.out.println(4 + 2 + " 小时");    // 6 小时
```

第一行的 `+` 从左到右执行：字符串与 4 拼接为 `总数：4`，然后再拼接 2。第二行先计算括号里的整数加法。第三行先算整数，再拼接文字。

更易读的写法是先计算，再输出：

```java
int total = 4 + 2;
System.out.println("总数：" + total);
```

输出正确之前，先保证表达式的中间值符合预期。这个习惯同样适用于以后写 SQL、价格计算和模型调用成本统计。

## 常见错误如何排查

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


## 综合练习

先独立预测并修改代码，再展开答案核对。

### A. 改成你的学习档案（必做）

把 `learner` 改为你希望使用的称呼。预测输出中哪一行变化，哪些行不变。重新编译和运行验证。

::: details 展开参考解释
只有“学习者”一行变化。计算依赖天数、每天时长和周数，姓名不参与计算。
:::

### B. 调整每天时间（必做）

保持每周 4 天、48 周，先把每天时间改成 3.0，再改成 2.0。

先填写，不运行：

| 每天小时 | 每周小时 | 48 周小时 |
|---:|---:|---:|
| 3.0 | | |
| 2.0 | | |

编译运行后，检查输出与计算是否一致。最后恢复为 2.5。

::: details 展开参考解释
| 每天小时 | 每周小时 | 48 周小时 |
|---:|---:|---:|
| 3.0 | 12.0 | 576.0 |
| 2.0 | 8.0 | 384.0 |

算式先按变量求值，结果再拼接成输出字符串。
:::

### C. 故意不重新编译（必做）

1. 确认当前 `.class` 对应每天 2.5 小时。
2. 只把源码中的 2.5 改为 3.0。
3. 不运行 Maven，只运行 `java -cp target/classes com.dailystudy.day001.LearningBudget`。
4. 预测它打印哪个值，并解释。
5. 再编译运行，比较结果。

这题关注源码与字节码的关系，不是让你长期跳过编译。

::: details 展开参考解释
只运行已有字节码，仍得到每天 2.5、每周 10.0、总计 480.0。重新编译后才得到每天 3.0、每周 12.0、总计 576.0。

这里讨论的是本课使用 `java -cp ... 类名` 的运行方式，它加载已经编译的类。Java 也有直接运行源文件的方式，本课暂不引入。
:::

### D. 判断并修复类型错误（必做）

临时把下面一行：

```java
double studyHoursPerDay = 2.5;
```

改成：

```java
int studyHoursPerDay = 2.5;
```

先预测能否编译，再执行 `mvn -B -ntp compile`，观察第一条相关编译错误并解释。不要在编译失败后把旧 `.class` 输出当作新代码成功。把类型改回 double，重新编译确认恢复。

::: details 展开参考解释
编译失败。2.5 是带小数的浮点字面量，不能直接赋给 int。常见诊断包括 `possible lossy conversion from double to int`。解决方式是保留 double，不为绕过错误而随意强制转换。

编译失败时可能仍残留上次成功的 `.class`。旧程序能运行不能证明当前源码可编译。
:::

### E. 参数之间的关系（扩展）

原始参数为每天 2.5 小时、每周 4 天、48 周。现在增加两周休息：

1. 用 `int breakWeeks = 2;` 表示休息周数。
2. 用变量表示实际学习周数，不把 46 写死在计算式里。
3. 计算实际总学习时长。
4. 保留原有计划值，同时打印实际值。

本课不用处理所有非法参数，输入校验会在后面的控制流课加入。

::: details 展开参考解释
可在原有计算之后增加：

```java
int breakWeeks = 2;
int actualWeeks = totalWeeks - breakWeeks;
double actualHours = weeklyHours * actualWeeks;
System.out.println("实际学习周数：" + actualWeeks);
System.out.println("实际学习小时：" + actualHours);
```

原始参数下是 46 周、460.0 小时。如果修改 totalWeeks，这一计算应跟着变化。
:::

### F. 整数除法（扩展）

在 main 中加入：

```java
System.out.println(5 / 2);
System.out.println(5.0 / 2);
```

先预测，再运行。结合 int 与 double 解释与 JavaScript 数字运算的差异。

::: details 展开参考解释
```text
2
2.5
```

两个操作数都是整数时执行整数除法，本题结果为 2；出现浮点操作数时，执行浮点运算。本题使用正数，后续再讨论更完整的算术规则。
:::

## 理解检查

- `java -version` 和 `javac -version` 都显示 Java 21。
- `mvn -version` 显示使用 Java 21。
- 我能手动编译并运行 HelloStudy。
- 我能运行 Maven 项目并得到 10.0 与 480.0。
- 我能修改每天学习时长，预测结果并验证。
- 我能说明源码、字节码、JVM 的关系。
- 我能说明编译与运行为什么是两步。
- 我知道运行命令所在目录和 classpath 的作用。

如果计算器运行了，但你无法解释变量、计算或重新编译，就再做一次独立修改。


## 延伸阅读

1. [Temurin JDK 21 下载](https://adoptium.net/temurin/releases/?version=21)：选操作系统和架构；不要选只有运行能力的包。
2. [Java 官方 Getting Started](https://dev.java/learn/getting-started/)：配合源码、编译、运行的讲解阅读。
3. [Java 21 javac 命令说明](https://docs.oracle.com/en/java/javase/21/docs/specs/man/javac.html)：本课只查 encoding、d。
4. [Apache Maven 安装说明](https://maven.apache.org/install.html)：安装和检查；不需要现在学完整生命周期。
5. [VS Code Java 入门](https://code.visualstudio.com/docs/java/java-tutorial)：已有编辑器经验时快速设置。
6. [B 站视频检索：Java 21 JDK 安装](https://search.bilibili.com/all?keyword=Java%2021%20JDK%20%E5%AE%89%E8%A3%85)：这是检索入口，未指定或背书单个视频；只补看安装或 Hello World 对应小节。

学习先读本课，只有卡住或想核对细节时再打开资料。无需看完全部入口才动手。


## 下一层知识

[HTTP 请求与响应](/lessons/http) 会把终端程序延伸到网络：服务端持续等待请求，前端与它通过接口契约交互。
