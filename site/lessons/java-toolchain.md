---
description: Java 程序的基本结构，源码与字节码，javac/java 的用法，以及包、classpath 与 Maven 的入门说明。
---

# Java 01 · 程序结构、编译与运行

Java 的入门程序通常先写一个类，再在 main 方法里写要执行的语句。我们先运行一个最小程序，然后用它解释文件名、类名、编译和运行之间的关系。

本章采用 Java 21。只需要已有的 JDK，不涉及操作系统安装步骤。主要参考 [Java 官方入门章节](https://github.com/java/devjava-content/blob/main/app/pages/learn/01_tutorial/01_your-first-java-app/01_getting-started-with-java.md)。

## 第一个程序

新建 `Hello.java`，注意文件名大小写：

```java
public class Hello {
    public static void main(String[] args) {
        System.out.println("你好，Java！");
    }
}
```

在文件所在目录执行：

```bash
javac -encoding UTF-8 Hello.java
java Hello
```

输出：

```text
你好，Java！
```

第一条命令会生成 Hello.class；第二条命令运行这个类。这里传给 java 的是类名 Hello，既不是 Hello.class，也不是文件路径。

仓库也有相同形式的示例 `lessons/day-001/examples/HelloStudy.java`。以下命令从仓库根目录执行：

```bash
javac -encoding UTF-8 -d lessons/day-001/out lessons/day-001/examples/HelloStudy.java
java -cp lessons/day-001/out HelloStudy
```

`-d` 指定编译结果目录，`-cp` 指定运行时查找类的位置。两条命令使用同一个目录，是因为运行工具要找到刚刚生成的 class。

## 阅读源码

先看类的声明：

```java
public class Hello {
    // 类的内容。
}
```

class 表示声明一个类，Hello 是类名。这个例子里，public 顶层类名必须与文件名相同。`{` 和 `}` 包围类的内容。

再看方法：

```java
public static void main(String[] args) {
    System.out.println("你好，Java！");
}
```

main 是这里的程序入口。程序启动后执行它里面的语句。public、static、void 与 String[] 的完整规则后续会分别学习，现在先认清各部分的作用：

| 部分 | 在这里的含义 |
|---|---|
| public | 方法公开可访问 |
| static | 方法属于类，调用入口前不需要先创建 Hello 对象 |
| void | 方法不返回结果值 |
| main | 入口方法的名字 |
| String[] args | 接收命令行参数 |
| System.out.println | 向标准输出打印内容，随后换行 |

打印文字不等于返回值。后面的语法章会比较 println 与 return。

语句 `System.out.println("你好，Java！");` 用分号结束。字符串使用英文双引号。把引号改成中文引号，或者漏掉分号，编译器会报告语法错误。

缩进不决定代码块，括号才决定。一般每层缩进四个空格，方便阅读：

```java
public class Hello {
    public static void main(String[] args) {
        System.out.println("第一行");
        System.out.println("第二行");
    }
}
```

这里先打印第一行，再打印第二行，执行顺序由语句顺序决定。

## 源码、字节码、JDK 与 JVM

编辑器里写的是 Java 源码，javac 将源码编译为字节码，JVM 执行字节码：

```text
Hello.java  →  javac  →  Hello.class  →  JVM 执行  →  输出
```

JDK 是开发工具包，包含编译器和运行程序需要的工具。JVM 是执行字节码的虚拟机。使用 `java` 命令时，它会启动 JVM 并加载指定的类。

检查当前工具版本：

```bash
java -version
javac -version
```

本项目二者使用 21。只有 java 可用而 javac 不可用，还不能完成这里的开发流程。

做一个实验：把 Hello.java 的问候改为“第二次运行”，直接执行 `java Hello`。结果仍可能是旧文字，因为 Hello.class 没有更新。重新运行 javac，再运行 java，才会使用新内容。

Java 21 也支持直接运行单个源码文件：

```bash
java Hello.java
```

这是源码文件启动模式，工具会完成相应的编译和执行，不要求你先手动生成 Hello.class。初学时先用两条命令，便于分别观察编译错误和运行错误。

## 变量与表达式

变量给数据起一个名字，后面的语句就可以使用这个名字：

```java
int count = 3;
int price = 20;
int total = count * price;
System.out.println(total);
```

这段放在 main 里，输出 60。第一行声明整数变量 count，并把 3 赋给它；第三行计算乘法，把结果赋给 total。

Java 声明变量时写类型。int 表示整数，String 表示字符串。变量名和字符串内容是两回事：

```java
String name = "小林";
System.out.println(name);   // 小林
System.out.println("name"); // name
```

引号里的是要使用的文字，没有引号的 name 是变量名。

数值相加和字符串拼接也不同：

```java
System.out.println(4 + 2);          // 6
System.out.println("4" + "2");      // 42
System.out.println("合计：" + 4 + 2); // 合计：42
System.out.println("合计：" + (4 + 2)); // 合计：6
```

`+` 从左到右结合。第三行先得到“合计：4”，随后与 2 拼接；第四行先算括号中的加法。涉及计算时，先存入一个结果变量，通常更容易读。

## 注释与命名

注释用于解释代码，不参与执行：

```java
// 单行注释。
/* 也可以写
   多行注释。 */
```

类名通常以大写字母开头，例如 OrderSummary；变量和方法名通常以小写字母开头，例如 itemCount、calculateTotal。名字区分大小写，itemCount 和 ItemCount 是两个名字。

不需要把每个字母都写成注释。像 `count = count + 1` 这种直观操作，注释可以省略；不明显的计算规则更值得说明。

字符串中的换行和引号需要转义：

```java
System.out.println("第一行\n第二行");
System.out.println("他说：\"你好\"");
```

`\n` 是换行，`\"` 是字符串里面的双引号。

## 命令行参数

main 的 args 可以接收启动命令后面的文字。新建 `Greeting.java`：

```java
public class Greeting {
    public static void main(String[] args) {
        String name = args.length == 0 ? "访客" : args[0];
        System.out.println("你好，" + name + "！");
    }
}
```

运行：

```bash
javac -encoding UTF-8 Greeting.java
java Greeting
java Greeting 小林
java Greeting "Lin Chen"
```

三次分别输出“你好，访客！”、“你好，小林！”、“你好，Lin Chen！”。双引号让 shell 把带空格的名字作为一个参数传入。

`args.length` 是参数个数，`args[0]` 是第一个参数。Java 数组从下标 0 开始。上面的条件表达式表示：没有参数用“访客”，有参数用第一个参数。数组和条件语句在下一章展开。

## 常见错误

| 现象 | 先检查什么 |
|---|---|
| javac 报找不到源文件 | 当前目录与文件路径 |
| public 类必须在对应文件中声明 | 文件名是否与公开类名一致 |
| 编译器指出某行语法错误 | 引号、分号、括号；也检查前一行 |
| java 找不到主类 | 类名、包名、classpath，是否已经编译 |
| UnsupportedClassVersionError | 运行环境是否比编译目标版本旧 |
| 改了文字，输出仍旧 | 是否重新编译，是否加载了正确目录的 class |

处理错误时先保留报错原文，再确认出错的命令。编译没通过时继续运行旧 class，可能让你以为新源码已经生效。

## 练习

### 1. 修改输出

让 Hello 打印两行：第一行你的名字，第二行“开始学习 Java”。检查需要几次 println，编译后再运行。

::: details 参考写法
把下面两句放到 main 中：

```java
System.out.println("小林");
System.out.println("开始学习 Java");
```
:::

### 2. 计算总价

在 main 中声明单价 15、数量 4，计算总价，输出“总价：60”。然后把数量改为 5，重新编译运行。

::: details 参考写法
```java
int price = 15;
int count = 4;
int total = price * count;
System.out.println("总价：" + total);
```

本例使用整数单价，不涉及浮点金额。数量改为 5 时总价为 75。
:::

### 3. 区分编译与运行

编译成功后，把源码中的分号删掉，再执行 java Hello，接着执行 javac Hello.java。为什么两条命令的结果不同？

::: details 解释
java Hello 加载的是已有 class，所以仍可能正常运行。javac 读取修改后的源码，会发现语法错误。运行成功不代表当前源码能通过编译。
:::

## 附录：包与 Maven

这部分用于运行仓库项目，第一次阅读可以先到这里，继续下一章。程序拆成多个类以后再回来完整练习。

### 包与完整类名

```java
package example;

public class PackagedHello {
    public static void main(String[] args) {
        System.out.println("包中的程序");
    }
}
```

把源码保存为 PackagedHello.java，然后执行：

```bash
javac -encoding UTF-8 -d out PackagedHello.java
java -cp out example.PackagedHello
```

编译结果在 out/example/PackagedHello.class，运行时使用完整类名 example.PackagedHello。classpath 指向包目录的根 out，不是 out/example。

package 是命名空间。import 让源码可以用短名字引用其他包的类型，它不负责下载依赖。

### Maven 的基本用法

Maven 按 pom.xml 中的配置完成编译、依赖管理、测试与打包。常见目录如下：

```text
项目目录/
  pom.xml
  src/main/java/    应用源码
  src/test/java/    测试源码
  target/          构建结果
```

在仓库根目录运行现有 Java 项目：

```bash
mvn -B -ntp -f projects/java-foundations/pom.xml test
```

- `-f` 指定项目的 pom.xml。
- `test` 会执行之前的编译阶段，然后运行测试。
- `-B` 使用非交互方式，`-ntp` 减少下载进度输出。

后续打包使用 package；它会执行测试并生成 JAR。JAR 是打包文件，不保证自带运行入口或所有依赖，只有满足配置要求时才能直接用 java -jar 启动。

## 阅读资料

- [Java 官方 Getting Started](https://github.com/java/devjava-content/blob/main/app/pages/learn/01_tutorial/01_your-first-java-app/01_getting-started-with-java.md)：对应本章编译、执行和源码文件启动。
- [MOOC：Printing](https://github.com/rage/java-programming/blob/master/data/part-1/2-printing.md)：多做几道输出与语法练习。
- [Javaer：第一个程序](https://github.com/itwanger/toBeBetterJavaer/blob/master/docs/src/overview/hello-world.md)：中文补充阅读。

[下一章：基本语法、数组与方法](/lessons/java-control-flow)。
