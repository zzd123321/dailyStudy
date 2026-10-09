---
description: 先让 Java 打印一句话，再用具体命令理解源码、编译、运行、JDK、JVM 与 Maven。
---

# 01 · Java 工具链与程序执行

你可能写过这样的 JavaScript：

```javascript
console.log('你好，Java！');
```

换成 Java，我们仍然先做同一件事：把一句话打印到终端。然后追问两个问题：这段代码由谁执行？为什么改完代码，运行结果有时没变？

本课先把程序跑起来，再解释 JDK、JVM 和 Maven。安装细节放在 [macOS 开发环境](/setup)，这里重点学会读命令和理解结果。

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
| String[] args | 可接收命令行参数，当前不使用 |
| 大括号 | 标明类、方法或其他代码块的范围 |

先能说明入口在哪里，不要求一次掌握对象和静态方法的全部规则。

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

## Maven：帮你按项目配置完成构建

小文件可以直接 javac。项目大了，还需要编译多个文件、下载依赖、运行测试和打包；Maven 帮你组织这些步骤。

它读 `pom.xml`。可以先把它和前端项目的 package.json 作粗略对照：都是说明项目与依赖的配置，但两套工具的规则不同。

当前在有 pom.xml 的 `projects/java-foundations` 目录执行：

```bash
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

## 配套阅读

正文使用仓库自己的例子，组织方式参考公开入门教程，以下按主题选读：

- [二哥的 Java 进阶之路：第一个程序](https://github.com/itwanger/toBeBetterJavaer/blob/master/docs/src/overview/hello-world.md)：可读代码解释；其中 IDE 与 JDK 版本不同，本仓库继续使用 Java 21 与 Mac 命令。
- [Java 官方 Getting Started](https://dev.java/learn/getting-started/)：核对编译与运行。
- [javac 参数说明](https://docs.oracle.com/en/java/javase/21/docs/specs/man/javac.html)：查 encoding 和 d。
- [Maven 入门指南](https://maven.apache.org/guides/getting-started/)：遇到构建概念再查。

[继续第 02 课：HTTP 请求与响应](/lessons/http)。
