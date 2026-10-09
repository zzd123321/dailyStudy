---
description: 先让 Java 打印一句话，再用具体命令理解源码、编译、运行、JDK、JVM 与 Maven。
---

# 01 · Java 工具链与程序执行

你可能写过这样的 JavaScript：

```javascript
console.log('你好，Java！');
```

换成 Java，我们仍然先做同一件事：把一句话打印到终端。然后追问两个问题：这段代码由谁执行？为什么改完代码，运行结果有时没变？

本课先把程序跑起来，再解释 JDK、JVM 和 Maven，最后把程序组织成包并打包运行。示例使用 Java 21，重点学会读命令、理解结果与排查构建问题。

## 先确认工具，再运行一句话

在终端输入：

```bash
java -version
javac -version
mvn -version
```

java 和 javac 都应是 21，Maven 输出里也应显示使用 Java 21。**javac 是编译工具，java 是启动程序的工具**，所以两个都要检查。

首次使用仓库：

```bash
git clone https://github.com/zzd123321/dailyStudy.git
cd dailyStudy
```

已经有仓库时，先保存自己的改动，再用 `git pull --ff-only origin main` 更新，不必重复克隆。

打开 `lessons/day-001/examples/HelloStudy.java`：

<<< @/../lessons/day-001/examples/HelloStudy.java

先看中间这一行：

```java
System.out.println("你好，Java！");
```

它类似 console.log：把括号里的文字显示到终端，然后换行。文字用英文双引号包起来，分号结束这条语句。

从仓库根目录执行：

```bash
cd lessons/day-001
javac -encoding UTF-8 -d out examples/HelloStudy.java
java -cp out HelloStudy
```

应看到 `你好，Java！`。不要一次就背下所有参数，先知道前一条编译、后一条运行。

## 为什么用了两条命令

你编辑的是 `HelloStudy.java`，但这次运行命令加载的是编译后的文件：

```text
你写的文件                   编译得到的文件             输出
HelloStudy.java ── javac ──→ out/HelloStudy.class ── java ──→ 你好，Java！
```

源码是你能编辑的 Java 文字；字节码是编译后交给 Java 运行环境的形式。本课的运行方式需要先编译，再执行。

把问候改成自己的名字，**先不编译，只运行第二条命令**。你会发现还打印旧问候，因为旧 class 没变。再执行编译和运行，才看到新文字。

这和修改 TypeScript 源码，却仍打开旧构建产物，是同类问题。它帮助你记住：改源码，不等于运行中的内容已经更新。

### 把编译命令拆开读

```bash
javac -encoding UTF-8 -d out examples/HelloStudy.java
```

- javac：使用 Java 编译器。
- encoding UTF-8：源文件按 UTF-8 解读，中文不乱解码。
- d out：编译结果放到 out 目录。
- 最后一个路径：要编译哪个源文件。

运行命令也拆开读：

```bash
java -cp out HelloStudy
```

意思是“从 out 目录查找 HelloStudy 这个类，再执行它的入口”。cp 是 classpath，即**查找已编译类的位置**。这里写类名，不加 .java 或 .class 后缀。

命令里的相对路径从当前目录开始计算。找不到文件时，先用 `pwd` 看自己在哪，再用 `ls` 看目录内容。

## 外面的 class 和 main 是做什么的

把 HelloStudy 想成这个文件里定义的一个程序单元。Java 用 class 声明它；类的细节在后面学，先保持公开类名与文件名一致。

```java
public class HelloStudy {
    public static void main(String[] args) {
        // 程序从这里开始执行。
    }
}
```

在本课的写法中，main 是程序入口。你希望启动后马上执行的语句，就放在 main 的大括号里。

| 写法 | 先这样理解 |
|---|---|
| public class HelloStudy | 定义公开类 HelloStudy，文件名对应 HelloStudy.java |
| main | 这个程序使用的入口方法 |
| static | 启动入口前不需要先创建这个类的对象 |
| void | 这个方法不返回结果值，打印和返回是两件事 |
| String[] args | 可接收命令行参数，后面会实际传入名字 |
| 大括号 | 标明类、方法或其他代码块的范围 |

先能说明入口在哪里，不要求一次掌握对象和静态方法的全部规则。

## 先读懂源码的基本结构

运行成功之后，回到文件本身。Java 程序里的文字大致分为四种：声明、语句、表达式和注释。

```java
int days = 4;                         // 声明一个变量，并给初始值。
double weeklyHours = days * 2.5;       // 乘法表达式算出一个值。
System.out.println(weeklyHours);      // 执行一条打印语句。
```

**变量**让一个值有名字，**表达式**算出一个值，**语句**完成一步操作。分号结束的是语句，不是每一行文字；类和方法的大括号后通常不写分号。

下面两种注释都不会成为计算步骤：

```java
// 单行注释：解释为什么选择这个规则。
/* 多行注释：可以跨行说明一段代码的用途。 */
```

命名也有习惯：类用 `LearningBudget`，变量和方法用 `weeklyHours`、`calculateWeeklyHours`。名字区分大小写，`weeklyHours` 与 `WeeklyHours` 不是同一个变量。尽量说明用途，而不是全部叫 a、b、c。

### 文字、数字和变量名有什么区别

在 main 内试这段代码：

```java
int days = 4;
System.out.println(days);       // 4：读取变量的值。
System.out.println("days");     // days：打印这四个字母。
System.out.println(4 + 2);      // 6：数字相加。
System.out.println("4" + "2");  // 42：文本拼接。
```

`"4"` 是字符串字面量，`4` 是整数值；外观相似，类型与运算规则不同。Java 不会因为文字看起来像数字，就在这里自动把它变成数字。

文本里需要双引号或换行时，使用转义：

```java
System.out.println("他说：\"开始学习\"");
System.out.println("第一行\n第二行");
```

`\"` 表示字符串内部的双引号，`\n` 表示换行。它们属于 Java 字符串语法，不是让终端执行某条命令。

现在能把一个程序读成“准备哪些值 → 算什么 → 展示什么”。第 03 课会进一步让这些值来自用户输入，并加入判断与循环。

## JDK 与 JVM：名字不同，职责也不同

刚才你已经用了编译器和运行器，现在再给这些能力起名字：

- **JDK：Java 开发工具包。** 你安装的开发工具集合，包含 javac 和运行 Java 所需的能力。
- **JVM：Java 虚拟机。** 运行时负责加载并执行字节码的部分。

可以把 JDK 理解为装好的开发工具箱，JVM 理解为其中负责执行程序的机器。这个比喻帮助区分职责，不表示它们是两台真实电脑。

只装能运行 Java 的环境，可能没有 javac。这就是为什么“java -version 成功”仍不足以证明开发工具齐全。

不同系统使用适合自己的 JDK；同一份字节码在兼容的 Java 环境中执行。系统架构、Java 版本和本地依赖仍需匹配，不是所有文件随便拷贝都能运行。

## 从一句话到一个计算器

从 `lessons/day-001` 返回仓库根目录，再进入项目：

```bash
cd ../..
cd projects/java-foundations
```

打开 `src/main/java/com/dailystudy/day001/LearningBudget.java`。先只看输入和计算：

```java
int studyDaysPerWeek = 4;
double studyHoursPerDay = 2.5;
int totalWeeks = 48;

double weeklyHours = studyDaysPerWeek * studyHoursPerDay;
double plannedHours = weeklyHours * totalWeeks;
```

每行的中文意思是：

1. 每周学 4 天；天数用整数 int。
2. 每天学 2.5 小时；可能有小数，用 double。
3. 计划 48 周。
4. 每周小时 = 4 × 2.5，得到 10。
5. 总小时 = 10 × 48，得到 480。

Java 把类型写在变量名前面。与你熟悉的 TypeScript 对照：

```text
TypeScript：const days: number = 4;
Java：      int days = 4;
```

Java 分开表示整数和浮点数，不能把 2.5 直接放进 int。double 也有浮点精度限制，金额计算会单独学习。

::: details 为什么“总数：” + 4 + 2 得到 42
`+` 在这个表达式中从左到右结合。文字加 4 先变成“总数：4”，再加 2 就变成“总数：42”。

```java
System.out.println("总数：" + 4 + 2);    // 总数：42
System.out.println("总数：" + (4 + 2));  // 总数：6
```

清楚的写法是先算结果，再拼接文字。
:::

## 把文件、包名和类名对应起来

HelloStudy 没有包名，所以运行时只写 HelloStudy。LearningBudget 有包名，所以运行时写完整名称。这个区别不是 Maven 随意规定的，而是 Java 的命名与查找规则。

包可以理解成类的命名空间。例如 `study.Greeting` 和 `other.Greeting` 可以是两个不同的类，就像两个模块里可以有同名组件。

下面亲手做一次。**在仓库根目录以外找一个练习目录**，把以下完整代码保存为 `src/study/Greeting.java`，目录与包名对应：

```java
package study;

public class Greeting {
    public static void main(String[] args) {
        System.out.println("你好，" + args[0] + "！");
    }
}
```

在这个练习目录执行：

```bash
javac -encoding UTF-8 -d out src/study/Greeting.java
java -cp out study.Greeting 小林
```

你会看到 `你好，小林！`。现在三个名称可以一一对应：

```text
源文件：   src/study/Greeting.java
编译产物： out/study/Greeting.class
完整类名： study.Greeting
```

classpath 是 `out`，因为 JVM 在它下面继续找 `study/Greeting.class`。不能把 classpath 设成最里面的 `out/study`，再仍然要求它查找完整类名。

### main 的 args 原来真的能接收数据

命令中的“小林”进入了 main 的 `args`。`String[]` 表示一组字符串，`args[0]` 是第一个元素；下标从 0 开始。

这份最小示例要求你**提供至少一个参数**。不提供时，访问不存在的第一个元素会产生运行异常。第 03 课会学习数组、长度判断与输入校验，届时可以给它补上友好的错误提示。

多词参数要让终端把它作为一个整体，例如：

```bash
java -cp out study.Greeting 'AI 学习者'
```

引号用于 shell 分组，不属于传进去的名字。把它与 Java 源码中的字符串引号区分开：前者控制命令怎样被拆分，后者定义程序里的文本。

## 从 class 文件到 JAR：怎样把程序交给别人

现在你有编译后的类。JAR 是把类和资源整理到一起的归档格式；它本身不保证“可执行”，还需要说明入口，或者通过 classpath 指定类。

继续在上面的 Greeting 练习目录执行：

```bash
jar --create --file out/greeting.jar --main-class study.Greeting -C out study
jar --list --file out/greeting.jar
java -jar out/greeting.jar 小林
```

第一条命令把 `out/study` 中的类装进 JAR，并记录入口；第二条查看包内内容；第三条运行。你应看到 `study/Greeting.class` 和 `META-INF/MANIFEST.MF`，再次输出问候。

| 部分 | 含义 |
|---|---|
| `--create` | 创建归档 |
| `--file` | 输出到哪个 JAR 文件 |
| `--main-class` | 指定启动入口 |
| `-C out study` | 临时从 out 目录收集 study 目录，保留包路径 |

这个练习只有一个类，没有第三方依赖，因此很容易交付。别人仍要有兼容的 Java 运行环境。

### 为什么 Maven 打出了 JAR，java -jar 却可能失败

普通 JAR 可能没有入口清单；有入口的 JAR 也可能缺运行依赖。`mvn package` 与“生成一个包含所有依赖的可执行包”不是同一回事。

当前 java-foundations 使用 classpath 启动；http-playground 还需要 `target/dependency/*` 中的 Jackson。后续 Spring Boot 的打包插件会处理另一种可执行包结构，到时再比较。

先分清三件事：**编译出了类，归档了产物，具备完整启动条件**。不能拿其中一个替代另一个。

## Maven：帮你按项目配置完成构建

小文件可以直接 javac。项目大了，还需要编译多个文件、下载依赖、运行测试和打包；Maven 帮你组织这些步骤。

它读 `pom.xml`。可以先把它和前端项目的 package.json 作粗略对照：都是说明项目与依赖的配置，但两套工具的规则不同。

完成独立练习后，回到仓库根目录，再进入有 pom.xml 的项目目录执行：

```bash
cd projects/java-foundations
mvn -B -ntp compile
java -cp target/classes com.dailystudy.day001.LearningBudget
```

先编译，再运行计算器。你应看到每周 10.0 小时、48 周 480.0 小时。

Maven 默认把主源码编译到 target/classes。类开头的 package 为它指定了包名，相当于命名空间；所以运行时需要完整类名 `com.dailystudy.day001.LearningBudget`。

::: details 为什么 classpath 是 target/classes，而不是最里面的包目录
JVM 从根目录加上包路径查找：

```text
target/classes + com/dailystudy/day001/LearningBudget.class
```

因此 cp 指向根目录，后面写完整类名。不要省略包名，也不要把源文件路径直接当成类名。
:::

`mvn compile` 不会自动运行这个 main，所以后面仍有一条 java 命令。B 表示非交互模式，ntp 隐藏下载进度，不改变你的计算。

::: details 后面会用到的 Maven 阶段
compile 编译；test 在编译后运行测试；package 测试并打包。clean 属于另一个生命周期，主要删除构建产物。

`mvn clean test` 是先清理再测试。install 把构建产物放进本机 Maven 仓库，deploy 发布 Maven 产物到远程仓库；这两个名称不表示自动把业务网站部署到服务器。

项目依赖是代码使用的库；构建插件是参与编译或测试的工具。现在知道这两个职责不同即可。
:::

## 会读 pom.xml，才知道 Maven 在替你做什么

打开 java-foundations 的 pom.xml，先识别下面这几项。以下是从项目摘出的配置片段，省略了其他节点：

```xml
<groupId>com.dailystudy</groupId>
<artifactId>java-foundations</artifactId>
<version>0.1.0-SNAPSHOT</version>
<properties>
    <maven.compiler.release>21</maven.compiler.release>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
</properties>
```

前三项共同标识一个 Maven 产物，类似库的名称与版本。`SNAPSHOT` 表示开发中的版本；项目对外发布时需要明确版本策略。

`release=21` 告诉编译插件以 Java 21 的语言与 API 范围编译，不能因为你的电脑安装了更高版本，就随意在项目中使用更新的语法和 API。UTF-8 则明确源码编码。

### Maven 的目录约定

```text
projects/java-foundations/
├── pom.xml                 构建与依赖配置
├── src/main/java/          正式 Java 源码
├── src/test/java/          测试源码
└── target/                 编译、测试和打包产物
    ├── classes/            主源码编译结果
    ├── test-classes/       测试编译结果
    └── surefire-reports/   测试执行报告
```

你应编辑 src 中的源码，而不是 target 中的 class。target 可以由构建再生成，所以通常不进入 Git；这与前端项目的源码、dist 和依赖目录分工相似。

`mvn test` 不只是“下载测试框架”，它还会编译主源码、编译测试，再实际执行测试。看到 BUILD SUCCESS 后仍要读测试统计：运行了多少、失败多少、跳过多少。**零个测试不代表功能已经被验证。**

### 依赖与插件，各自做什么

项目使用 JUnit，配置中有 `scope=test`。意思是这个库用于测试，不应成为正式程序的运行依赖。

Jackson 则是 HTTP 服务解析和生成 JSON 所用的库，服务运行时也需要。依赖会有自己的依赖，这叫传递依赖，所以项目用一个库，最终可能下载多个 JAR。

编译插件、测试插件属于构建工具：负责“怎样编译、怎样执行测试”。它们与应用代码调用的库承担不同职责，不能只看名字都写在 pom.xml 就混为一谈。

Maven 下载的产物保存在本地仓库，后续可复用。缓存里有依赖，不表示项目源码已经编译；有编译产物，也不表示你已经运行最新源码。

### 用一次干净构建确认自己没有依赖旧产物

在 `projects/java-foundations` 执行：

```bash
mvn -B -ntp clean package
java -cp target/classes com.dailystudy.day001.LearningBudget
```

clean 清理本项目构建产物；package 按生命周期完成编译、测试和打包。命令结束后再运行计算器，仍应得到每周 10.0、总计 480.0。

如果只改源码、没有重新编译，就可能继续运行旧 class；如果编译失败还运行旧 class，也可能误以为新实现正确。干净构建可以排除这一类干扰，但不需要每改一行都清掉所有缓存。

## 出错时，先判断是哪一步

| 现象 | 先检查 |
|---|---|
| javac 找不到 | 是否安装 JDK，终端是否重新打开 |
| file not found | 当前目录与文件路径 |
| 公开类名和文件名不一致 | 大小写与名称 |
| Could not find or load main class | 是否编译、classpath、完整类名 |
| 改代码后仍显示旧结果 | 是否重新编译、是否运行了另一份输出 |
| Maven 找不到 POM | 是否进入有 pom.xml 的目录 |

**编译错误**是源码没通过检查，例如把 2.5 赋给 int；**运行错误**是开始执行后出问题，例如整数除以变量值 0。能编译，不代表所有输入都能正确处理。

## 动手练习：每次只改一件事

### 1. 改问候，不编译，再编译

修改 HelloStudy 的文字，先只运行旧 class，再编译运行。解释两次为什么不同。

::: details 参考解释
第一次加载旧字节码，所以还是旧文字。javac 生成新字节码后，才打印修改后的内容。
:::

### 2. 改计算器的每天小时

保持每周 4 天、48 周，把每天小时改为 3.0，再改为 2.0。先预测每周和总小时，再编译运行核对，最后恢复 2.5。

::: details 参考结果
3.0 得到每周 12.0、总计 576.0；2.0 得到每周 8.0、总计 384.0。
:::

### 3. 临时制造类型错误

把 `double studyHoursPerDay = 2.5;` 改成 `int studyHoursPerDay = 2.5;`，观察编译错误，再改回。

::: details 参考解释
int 不能直接接收这个小数。编译失败后可能仍有旧 class，旧程序能运行不代表新源码成功。
:::

### 4. 加入两周休息

新增 breakWeeks=2，计算 actualWeeks=totalWeeks-breakWeeks，再根据实际周数计算总小时。不要把 46 写死。

::: details 参考写法
在 main 内已有变量之后加入：

```java
int breakWeeks = 2;
int actualWeeks = totalWeeks - breakWeeks;
double actualHours = weeklyHours * actualWeeks;
System.out.println(actualHours); // 原始参数得到 460.0
```
:::

如果你能脱离示例改参数、重新编译并解释结果，就可以继续。下一课会让程序持续接收 HTTP 请求。

## 综合实践：独立交付一个带名字的问候程序

使用本课 Greeting 示例，完成一个完整闭环：

1. 把包名改成 `practice`，同时调整源文件目录。
2. 保持类名 Greeting；运行时传入自己的名字，输出“你好，名字！准备学习 Java。”。
3. 从源码编译到 out，再生成带入口的 JAR，分别通过 classpath 和 JAR 运行。
4. 故意漏写运行类的包名，读报错，再修正。
5. 修改问候但不编译，观察旧结果；重新编译并重新打包，确认 JAR 也得到新结果。

::: details 参考步骤与排错依据
源码第一行改为 `package practice;`，文件放在 `src/practice/Greeting.java`。核心语句是：

```java
System.out.println("你好，" + args[0] + "！准备学习 Java。");
```

在练习目录执行：

```bash
javac -encoding UTF-8 -d out src/practice/Greeting.java
java -cp out practice.Greeting 小林
jar --create --file out/greeting.jar --main-class practice.Greeting -C out practice
java -jar out/greeting.jar 小林
```

漏写包名，JVM 会在错误的位置找 Greeting；修改 class 后不重新打包，JAR 中仍是旧类。源码、class 和 JAR 是三份不同阶段的内容，不会自动互相同步。
:::

完成后，应能解释“源码在哪里、类名是什么、从哪里查找、启动入口在哪里、运行的产物是哪份”。这套问题会一直用到后端服务部署。

## 配套阅读

正文使用仓库自己的例子，组织方式参考公开入门教程，以下按主题选读：

- [二哥的 Java 进阶之路：第一个程序](https://github.com/itwanger/toBeBetterJavaer/blob/master/docs/src/overview/hello-world.md)：可读代码解释；其中 IDE 与 JDK 版本不同，本仓库继续使用 Java 21。
- [Java 官方 Getting Started](https://dev.java/learn/getting-started/)：核对编译与运行。
- [javac 参数说明](https://docs.oracle.com/en/java/javase/21/docs/specs/man/javac.html)：查 encoding 和 d。
- [Javaer：Maven 构建与依赖](https://github.com/itwanger/toBeBetterJavaer/blob/master/docs/src/maven/maven.md)：配合 pom.xml 和生命周期部分阅读，配置版本以本项目为准。
- [Java 官方：包](https://dev.java/learn/packages/)：核对包名、目录和访问边界。
- [Java 21 jar 工具](https://docs.oracle.com/en/java/javase/21/docs/specs/man/jar.html)：查创建归档与 main-class。
- [Maven 入门指南](https://maven.apache.org/guides/getting-started/)：遇到构建概念再查。

[继续第 02 课：HTTP 请求与响应](/lessons/http)。
