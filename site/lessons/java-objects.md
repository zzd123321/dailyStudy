---
description: 从任务对象学习字段、实例方法、构造器、this、封装、引用、参数传递与 static，并运行完整 Task 示例。
---

# Java 03 · 类、对象与封装

数组能保存多份同类数据，但一条任务本身就有几项不同的数据：ID 是整数，标题是字符串，完成状态是 boolean。类可以把这些数据和操作放在一起。

本章参考 [MOOC 的面向对象入门](https://github.com/rage/java-programming/blob/master/data/part-4/1-introduction-to-object-oriented-programming.md)与 [Javaer 的类和对象](https://github.com/itwanger/toBeBetterJavaer/blob/master/docs/src/oo/object-class.md)。先建立类，再学习构造器和封装，不先讨论 Web 分层。

## 类与对象

先写一个简单类：

```java
class TaskDraft {
    String title;
    boolean completed;

    void complete() {
        completed = true;
    }
}
```

title 与 completed 是字段，保存对象的状态；complete 是实例方法，改变当前对象的状态。

类是类型定义，对象是按这个定义创建的具体实例。下面是完整的小程序，保存为 `TaskIntro.java`：

```java
class TaskDraft {
    String title;
    boolean completed;

    void complete() {
        completed = true;
    }
}

public class TaskIntro {
    public static void main(String[] args) {
        TaskDraft first = new TaskDraft();
        first.title = "阅读 Java 教材";
        TaskDraft second = new TaskDraft();
        second.title = "完成练习";

        first.complete();
        System.out.println(first.title + "：" + first.completed);
        System.out.println(second.title + "：" + second.completed);
    }
}
```

编译运行：

```bash
javac -encoding UTF-8 TaskIntro.java
java TaskIntro
```

输出：

```text
阅读 Java 教材：true
完成练习：false
```

两次 new 创建两个对象，每个都有自己的字段。first.complete 只改变第一个对象。它不会把第二个对象也标为完成。

这个文件有两个顶层类，只有 TaskIntro 是 public，文件名与它一致。TaskDraft 暂时只是教学版本，后面的项目使用 Task。

## 字段与局部变量

字段在对象中保存状态，方法里的局部变量只用于本次执行：

```java
void complete() {
    String message = "已完成";
    completed = true;
    System.out.println(message);
}
```

方法执行结束后，局部变量 message 不再能从外部使用；对象的 completed 状态仍然可以被后续方法读取。

字段有默认值：int 为 0，boolean 为 false，引用类型为 null。前例没有给 completed 赋初始值，所以它开始为 false。局部变量则必须在使用前确定赋值。

null 表示没有引用对象。若 title 是 null，调用 title.strip 会产生 NullPointerException；后面的构造器会拒绝这种无效标题。

## 构造器

前例先创建对象，再逐项赋值。漏写标题时，对象就没有可用的标题。构造器允许创建时提供必要数据：

```java
class TaskDraft {
    String title;
    boolean completed;

    TaskDraft(String title) {
        this.title = title;
    }
}
```

随后使用 `new TaskDraft("完成练习")`。构造器名字与类名相同，没有返回类型，连 void 也不写。`void TaskDraft(...)` 是普通方法，不是构造器。

### this

`this.title = title` 左边是当前对象的字段，右边是参数。字段与参数可以同名，用 this 区分。

写成 `title = title`，两边都指向参数，不会修改对象字段。也可以把参数命名为 initialTitle，不过同名参数加 this 是常见写法。

### 默认构造器

完全没有声明构造器时，编译器会提供一个默认构造器。声明了带参数构造器以后，不会再自动补无参构造器：

```java
TaskDraft task = new TaskDraft("完成练习");
// new TaskDraft(); // 当前版本没有无参构造器。
```

自己写的无参构造器与编译器提供的默认构造器不是同一个概念。

## 封装

如果字段直接开放，调用者可以绕开要求：

```java
first.title = "";
first.completed = false;
```

给字段加 private，让修改通过方法完成：

```java
private String title;
private boolean completed;

public String getTitle() {
    return title;
}

public boolean isCompleted() {
    return completed;
}
```

在这个例子中，private 字段由 Task 自己的代码读写，其他普通类不能直接访问。查询方法允许读取需要公开的信息。

封装不是“每个字段都配一套 getter/setter”。标题可以通过 rename 修改，完成状态可以通过 complete 与 reopen 修改，ID 不提供修改方法。方法名说明允许的操作。

本例采用两条产品规则：

- ID 是正整数，标题去除首尾空白后不能为空。
- 已完成任务不能改名，需要先重新打开。

第二条是这个练习采用的需求，不是 Java 的规定。需求变了，就修改 Task 内的规则和相应测试。

### 先检查，再修改

```java
public void rename(String title) {
    if (completed) {
        throw new IllegalStateException("已完成的任务不能修改标题，请先重新打开。");
    }
    this.title = normalizeTitle(title);
}
```

先检查状态，再验证新标题，最后赋值。normalizeTitle 抛出异常时，赋值没有发生，旧标题仍保留。

IllegalArgumentException 用于这里的非法参数，例如空标题；IllegalStateException 用于当前状态不允许操作，例如完成后的改名。异常会向调用方传播，调用方决定怎样展示错误。Task 本身不负责弹窗。

## 构造器重载

Task 支持默认未完成和指定初始状态两种创建方式：

```java
public Task(int id, String title) {
    this(id, title, false);
}

public Task(int id, String title, boolean completed) {
    // 校验并初始化。
}
```

同名、不同参数列表叫重载。两参数构造器通过 this(...) 调用三参数构造器，避免重复初始化代码。

在 Java 21 中，显式 this(...) 或 super(...) 必须是构造器第一条语句。本例将真正的检查放在被调用构造器里。

## 对象引用

```java
Task first = new Task(1, "阅读教材");
Task alias = first;
alias.complete();
System.out.println(first.isCompleted()); // true
```

变量保存对象的引用值。`alias = first` 复制引用值，没有复制对象，两个变量因此找到同一个 Task。

```text
first ─┐
       ├──→ Task：阅读教材，已完成
alias ─┘
```

重新 new 才会创建另一个对象：

```java
Task another = new Task(1, "阅读教材");
System.out.println(first == another); // false
```

即使 ID 与标题相同，也不能证明是同一个对象。当前 Task 没有重写 equals，所以 equals 也沿用 Object 的身份比较。ID 是业务数据，是否按 ID 判等需要另外设计；集合章节再学习 equals/hashCode。

### 参数传递

Java 参数传递都是值传递。传入对象时，复制的是引用值：

```java
static void finish(Task task) {
    task.complete();
}

static void replace(Task task) {
    task = new Task(99, "另一条任务");
}
```

在 main 中：

```java
Task task = new Task(1, "阅读教材");
finish(task);
System.out.println(task.isCompleted()); // true
replace(task);
System.out.println(task.getId());       // 1
```

finish 与调用者通过各自的引用访问同一个对象，因而修改状态可见。replace 只给方法中的局部参数重新赋值，没有把调用者的变量改成另一个引用。

这两个结果放在一起，才能区别“通过引用改变对象”与“改变调用者的引用变量”。补充阅读：[JavaGuide：值传递](https://github.com/Snailclimb/JavaGuide/blob/main/docs/java/basis/why-there-only-value-passing-in-java.md)。

## static 与 final

普通字段属于每个对象，static 成员属于类：

```java
private static final int MAX_TITLE_CODE_POINTS = 120;
private final int id;
private boolean completed;
```

标题上限是同一条类规则，用 static final 常量表达。id 在构造器中赋值后不能重新赋值，用 final 字段表达。completed 每条任务不同，不能写成共享的 static 字段。

final 引用变量也只能赋值一次，但它引用的对象不一定不可变：

```java
final Task task = new Task(1, "阅读教材");
task.complete(); // 允许：改变对象状态。
// task = new Task(2, "另一条"); // 不允许：重新赋引用。
```

normalizeTitle 不使用某个任务的实例字段，因此可以声明为 private static 辅助方法。main 也是 static，但 main 创建的 Task 仍然各自拥有状态。

## 完整 Task

项目文件仍在原有 `projects/java-foundations/src/main/java/com/dailystudy/day005/`。目录名沿用历史编号，正文现在属于 Java 03，不需要复制文件。

<<< ../../projects/java-foundations/src/main/java/com/dailystudy/day005/Task.java{java}

normalizeTitle 使用 codePointCount 限制最多 120 个 Unicode 码点。它与 String.length 的 UTF-16 单元计数不同，也不等于所有情况下用户看到的字符个数。这是项目明确选择的计数规则。

toString 返回对象的文字描述，方便调试。它重写了 Object 的方法，所以有 @Override；继承和重写在下一章说明。

### 运行项目示例

从仓库根目录执行：

```bash
mvn -B -ntp -f projects/java-foundations/pom.xml test
java -cp projects/java-foundations/target/classes com.dailystudy.day005.TaskDemo
```

示例源码：

<<< ../../projects/java-foundations/src/main/java/com/dailystudy/day005/TaskDemo.java{java}

先关注这组变化：创建 → 改名 → 完成 → 改名被拒绝 → 重新打开 → 再改名。被拒绝后，标题仍是修改前的值。

测试还检查空白标题、超长标题、独立实例、重复完成与失败后保留数据。它们检查的是任务规则，不只是在验证 getter 返回字段。

## 练习

### 1. 独立实例与别名

创建 first 和 second 两条任务，再写 `Task alias = first`。调用 alias.complete 后，分别查询三者的状态。

::: details 答案
first 和 alias 查询同一对象，均为 true；second 是另一个对象，仍为 false。两次 new 与两次赋值不是一回事。
:::

### 2. 状态规则

已完成任务先 rename 会怎样？如果先 reopen 再 rename 呢？被拒绝时，原标题应该改变吗？

::: details 答案
直接 rename 抛出 IllegalStateException，原标题保留。reopen 后可以修改为合法标题，状态仍为未完成。
:::

### 3. 查询方法

给 Task 增加 canRename，返回当前是否允许改名。rename 是否可以删掉自己的状态检查，要求调用者先调用 canRename？

::: details 参考写法
```java
public boolean canRename() {
    return !completed;
}
```

rename 仍需自己检查。调用者可能跳过查询，查询结果也不应被当成永久保证。
:::

### 4. 修改规则

如果现在允许完成后改名，应该改哪里？标题空白检查是否也要删掉？

::: details 答案
修改 rename 中的状态限制，并更新“完成后拒绝改名”的测试。标题仍须合法，所以 normalizeTitle 保留。需求变化不需要开放字段直接写入。
:::

### 5. 独立建模

写 Book 类：书名非空，页数为正整数，提供 getTitle、getPages 和 rename。构造两本书，修改一本，检查另一本文字不变。

::: details 实现提示
先写 private 字段与构造器；再给书名校验提取辅助方法，创建和 rename 都使用它。页数如果不允许变化，就声明 final，并只提供读取方法。不要把任务的“完成后不能改名”复制到书本需求中。
:::

## 对应阅读

- [MOOC：面向对象入门](https://github.com/rage/java-programming/blob/master/data/part-4/1-introduction-to-object-oriented-programming.md)：类、构造器、实例方法和字段，适合换一组小例子练习。
- [Javaer：构造器](https://github.com/itwanger/toBeBetterJavaer/blob/master/docs/src/oo/construct.md)：对照 this 与重载。
- [JavaGuide：值传递](https://github.com/Snailclimb/JavaGuide/blob/main/docs/java/basis/why-there-only-value-passing-in-java.md)：完成引用实验后阅读。

[下一章：继承、接口与多态](/lessons/java-collaboration)。
