---
description: 用一条任务串联类、对象、字段、构造器、this、封装、状态变化、引用传递与 static，完成可运行的任务模型。
---

# 05 · Java 类、对象与封装

第 03 课已经能用变量、数组和方法统计时长，第 04 课也理解了“数据在哪里、什么时候改变”。现在继续回答：**一条任务有 ID、标题和完成状态，怎样把这些数据与操作放在一起，并保证它始终符合规则？**

这一课沿着同一个例子推进：先创建两个任务，观察各自的状态；再用构造器保证初始数据有效；最后通过封装控制改名、完成和重新打开。对象引用、static 和 final 都放在你已经看到的行为上解释。

完整示例沿用 `projects/java-foundations`，正文、练习与答案仍在这一页，不需要学习记录或额外安装说明。

## 从几个变量，到一条完整任务

如果只用零散变量，两条任务可能写成：

```java
int firstId = 1;
String firstTitle = "理解类与对象";
boolean firstCompleted = false;

int secondId = 2;
String secondTitle = "练习构造器";
boolean secondCompleted = false;
```

这些变量确实能工作，但有几个实际问题：修改任务时要带哪些值？标题和状态是否对应同一条任务？如果有一百条，是继续复制一百套变量，还是维护几组下标必须同步的数组？

你在前端可能会把相关数据组织成一个对象：

```javascript
const task = { id: 1, title: '理解类与对象', completed: false };
```

Java 也可以组织相关数据，但本课先用 class 定义结构与行为，再用 new 创建实例。它不使用 JavaScript 的对象字面量语法。

| 名字 | 对应这个例子 |
|---|---|
| 类 class | 定义任务有什么数据、能做什么 |
| 对象 object / 实例 instance | 一条具体任务，拥有自己的状态 |
| 字段 field | id、title、completed |
| 方法 method | 完成、改名、重新打开等操作 |

类不是某一条任务本身。同一 Task 类可以创建多条任务，它们的字段值可以不同。

## 先运行最小例子：两个对象的状态互不影响

下面是完整程序。放在独立练习目录中，文件名为 `TaskIntro.java`。同一文件可以有多个顶层类，但这里仅 TaskIntro 是 public，文件名与它一致。

```java
class TaskDraft {
    int id;
    String title;
    boolean completed;

    void complete() {
        completed = true;
    }
}

public class TaskIntro {
    public static void main(String[] args) {
        TaskDraft first = new TaskDraft();
        first.id = 1;
        first.title = "理解类与对象";

        TaskDraft second = new TaskDraft();
        second.id = 2;
        second.title = "练习构造器";

        first.complete();
        System.out.println(first.title + "：" + first.completed);
        System.out.println(second.title + "：" + second.completed);
    }
}
```

在该练习目录执行：

```bash
mkdir -p out
javac -encoding UTF-8 -d out TaskIntro.java
java -cp out TaskIntro
```

输出：

```text
理解类与对象：true
练习构造器：false
```

两次 new 得到两个对象。first.complete 只修改 first 指向的任务，second 的状态仍然是 false。

这只是便于观察的教学版，字段没有保护，调用者还可以写 `first.id = -1`。后面会把它改成符合业务规则的版本。仓库中的正式示例使用 Task，不使用这个 TaskDraft。

## 拆开 new 和字段访问，读懂每一步

```java
TaskDraft first = new TaskDraft();
```

可以按三部分理解：

1. `new TaskDraft()`：创建一个 TaskDraft 对象，并完成初始化。
2. `first`：保存指向这个对象的引用值，之后用它找到对象。
3. 左边的 `TaskDraft`：声明这个变量所能引用的类型。

随后 `first.title` 访问这个对象的 title 字段，`first.complete()` 调用这个对象的方法。它们都先确定“哪个对象”，再确定“它的哪个成员”。

### 字段和局部变量的生命周期不同

```java
class ExampleTask {
    boolean completed;  // 字段：对象的状态。

    void complete() {
        String message = "已完成"; // 局部变量：本次方法执行使用。
        completed = true;
        System.out.println(message);
    }
}
```

completed 在对象创建后可以被多次操作访问；message 只在这次方法的作用域内使用。把状态声明成局部变量，不能自动让下一次调用记住它。

实例字段会有默认值：int 为 0、boolean 为 false、引用类型如 String 为 null。因此最小例子的第二条任务，没有手动给 completed 赋值，仍然是 false。

**局部变量则必须在读取前明确赋值。** 不能把字段的默认初始化规则直接套到方法里的局部变量上。

::: details null、未初始化与默认值的三个区别
`String title = null` 是有一个明确的引用值，但它没有指向对象；对它调用方法可能出现 NullPointerException。

对象的 String 字段没赋值时，默认也是 null。局部变量 `String title;` 则不能在未赋值时直接读取，编译器会拒绝。

默认值让语言有明确初始化行为，不代表这些值符合业务。例如任务的 id 默认 0，但我们的规则要求正整数，因此要在创建阶段检查。
:::

## 构造器：对象一创建，就应有合格的初始状态

教学版是先 new，再分别赋值。中间可能出现“有对象，但标题还没有”的状态。如果忘了最后一步，就会留下半成品。

因此，正式 Task 要求创建时提供必要数据：

```java
Task task = new Task(1, "理解类与对象");
```

下面是构造器的入门结构，**用于解释语法，不是最终 Task 的全部源码**：

```java
public Task(int id, String title) {
    this.id = id;
    this.title = title;
    this.completed = false;
}
```

构造器名称与类相同，没有返回类型，连 void 也不写。它的职责是完成对象初始化，而不是返回计算结果。

`new Task(1, "理解类与对象")` 会把参数交给对应的构造器。构造完成后，调用处才得到可以继续使用的对象引用。

### this：区分当前对象的字段和方法参数

构造器里有一个参数叫 title，类里也有一个字段叫 title：

```java
this.title = title;
```

左边是“当前对象的 title 字段”，右边是“这次传进来的 title 参数”。如果写成 `title = title`，通常只是在给参数赋它自己，字段没有因此得到正确值。

this 指向当前实例。first 的构造与方法调用中，this 对应 first 的对象；second 调用时则对应 second 的对象。

### 默认构造器，不等于永远存在的无参构造器

TaskDraft 没有声明构造器，编译器会为它提供默认无参构造器。**一旦你声明了构造器，就不会再自动补一个无参版本。**

正式 Task 只有要求参数的构造器，`new Task()` 会编译失败。这样可以明确要求调用者提供 id 和 title，而不是等后面某个 setter 才把对象补完整。

无参构造器也可以由开发者自己声明。它与“编译器在完全没有构造器声明时提供的默认构造器”应区分开。

## 封装：把允许的修改方式留给对象自己决定

最小例子允许外部直接修改所有字段：

```java
first.id = -1;
first.title = "   ";
```

如果每个调用者都自己写校验，很容易有人忘记。正式 Task 用 private 隐藏字段，提供明确的操作：

```java
private final int id;
private String title;
private boolean completed;
```

外部可以通过 getId、getTitle、isCompleted 读取，但不能直接给字段赋值。更改状态则调用 rename、complete、reopen。

封装不仅是“字段设成 private，再自动生成所有 getter/setter”。更重要的是：**把业务允许的变化定义成操作，让状态通过这些操作保持有效。**

| 数据或操作 | 本例约定 |
|---|---|
| id | 正整数，创建后不变 |
| title | 去首尾空白后非空，最多 120 个 Unicode 码点 |
| completed | 每条任务自己的完成状态 |
| rename | 未完成时允许；完成后先 reopen |
| complete | 将任务设为已完成，重复执行仍已完成 |
| reopen | 将任务设为未完成，重复执行仍未完成 |

这是一组为教学项目选择的业务规则，其他产品可能允许已完成任务直接改名。规则来自需求，不是 Java 语言强制要求。

### 为什么不用 setCompleted 和 setTitle 暴露任意赋值

`task.complete()` 清楚地表达一次业务操作；`task.setCompleted(true)` 更像直接设置一个数据位。简单数据传输对象可能有合理的 setter，但当前任务包含操作规则，所以选择明确的方法名。

同样，只有 getter、没有 setter 的 id，表达“调用者可以知道它是什么，但不能随意更换”。这样后面的查询、修改和保存才能围绕稳定标识组织。

### final：不可重新赋值，不是所有内容都不可变化

id 字段使用 final，只能完成一次初始化，之后不能更换值。title 和 completed 需要改变，所以没有使用 final。

如果局部变量写成 `final Task task = ...`，它不能重新指向另一条任务，但仍可以调用 task.complete 改变对象状态。**引用不能改指向，与对象不能改内容，是两个问题。**

## 校验放在创建和修改边界，而不是只放在页面

正式 Task 的构造器先检查 ID，再规范化标题。rename 也复用同一套标题校验，避免创建时允许一种规则、修改时又换一套。

```java
private static String normalizeTitle(String title) {
    if (title == null) {
        throw new IllegalArgumentException("任务标题不能为空。");
    }
    String normalized = title.strip();
    if (normalized.isEmpty() ||
            normalized.codePointCount(0, normalized.length()) > MAX_TITLE_CODE_POINTS) {
        throw new IllegalArgumentException("任务标题须为非空字符串，最长 120 个 Unicode 码点。");
    }
    return normalized;
}
```

先检查 null，再调用 strip；先判断结果是否合格，再返回。normalizeTitle 返回规范化文本，没有偷偷修改某一条任务的字段。

创建或修改失败时，代码抛出异常，而不是把标题默默改成“未命名”掩盖输入问题。调用者决定给用户怎样提示；对象负责确保规则。

### 先检查，再赋值，保证失败不会破坏原状态

rename 的核心逻辑是：

```java
public void rename(String title) {
    if (completed) {
        throw new IllegalStateException("已完成的任务不能修改标题，请先重新打开。");
    }
    this.title = normalizeTitle(title);
}
```

右边规范化成功后，左边才赋值。如果 normalizeTitle 抛异常，原 title 仍然保留。这与第 04 课“校验失败不写入笔记”是同一原则。

已完成的任务先被状态规则拒绝，还没有检查新标题。和 HTTP 实验一样，**校验顺序决定先看到哪个错误**。

| 异常 | 当前例子中的含义 |
|---|---|
| IllegalArgumentException | 传入参数不满足约定，例如空标题 |
| IllegalStateException | 当前对象状态不允许操作，例如完成后改名 |

这两种异常都不是 HTTP 状态码。将来由接口层按契约转换成响应；Task 本身不用知道 HTTP 的存在。

## 构造器重载：用一套校验支持两种初始化方式

新建任务默认未完成；还原一条已有记录时，可能需要指定完成状态。于是提供两个构造器：

```java
public Task(int id, String title) {
    this(id, title, false);
}

public Task(int id, String title, boolean completed) {
    // 检查 id、title，然后保存完整初始状态。
}
```

两个构造器参数列表不同，是重载。前一个通过 `this(...)` 委托给后一个，避免复制两遍校验。Java 21 中，这个显式构造器调用应放在构造器第一条语句。

注意 `this.title` 是访问字段；`this(...)` 是调用同类的另一个构造器，两者不是同一种用法。

```java
Task fresh = new Task(1, "新任务");
Task restored = new Task(3, "已经完成的任务", true);
```

restored 仍需满足 ID 与标题规则，创建之后也遵守相同的改名限制。带三个参数并不意味着可以绕开校验。

这个例子的 ID 由调用者提供，类不负责保证全系统唯一。后续的数据访问层和数据库会进一步处理唯一性、生成方式与跨实例问题。

## 实例成员与 static：哪些属于一条任务，哪些属于类

每条任务各有 title 和 completed，所以它们是实例字段。不能把 completed 写成 static，否则你表达的就是由该类各实例共享的一份状态，而不是每条任务的状态。

可以用这个对照理解：

| 成员 | 是否 static | 原因 |
|---|---|---|
| id、title、completed | 否 | 每个对象各自有值 |
| complete、rename、reopen | 否 | 修改当前对象，需要 this |
| MAX_TITLE_CODE_POINTS | 是，并且 final | 统一的固定规则，不属于某一条任务 |
| normalizeTitle | 是 | 只根据参数处理文本，不使用某个实例状态 |
| main | 是 | 作为程序入口，启动时不先要求创建入口类对象 |

static 方法里没有某个当前实例，不能直接使用 this 或直接访问实例字段。它可以接收一个 Task 参数，然后通过该参数操作具体对象。

例如下面的方法属于演示类，参数明确指出要完成哪条任务：

```java
private static void completeTask(Task task) {
    task.complete();
}
```

static 不是“让程序更快”的开关，也不是“所有工具代码都应该这样写”。按状态归属选择；不要把用户、请求或任务数据随手放进共享静态变量。

## 引用：两个变量为什么可能改到同一条任务

以下片段使用正式 Task 类：

```java
Task first = new Task(1, "任务一");
Task alias = first;
alias.complete();
System.out.println(first.isCompleted()); // true。
```

第二行没有 new，也没有复制对象，只把引用值复制给 alias：

```text
first ──┐
        ├──→ 同一个 Task 对象：id=1，completed=true
alias ──┘
```

因此通过 alias 完成，first 也能看到变化。它们是两个变量，指向同一个实例。

与之对照：

```java
Task first = new Task(1, "任务一");
Task another = new Task(1, "任务一");
System.out.println(first == another); // false。
```

即使字段目前相同，两次 new 仍创建两个实例。`==` 在这里比较是否指向同一个对象，不是比较业务字段。

### Java 按值传递，也适用于对象参数

第 03 课的 increase(int value) 改参数，不会改 main 的原整数。对象参数同样复制的是值，只是这个值是引用。

```java
private static void completeTask(Task task) {
    task.complete();
}
```

方法参数拿到一份指向同一对象的引用值，所以修改对象状态能被调用者观察到。

如果在方法里改成 `task = new Task(99, "另一条")`，只是让方法的局部参数改指向，不会替调用者重新赋值原变量。

```text
传参之前：调用者变量 ──→ 对象 A
传参之后：调用者变量 ──→ 对象 A ←── 方法参数
参数改指向后：调用者变量 ──→ 对象 A；方法参数 ──→ 对象 B
```

这不能简称为“Java 对象是按引用传递”，那样很容易误以为方法能替调用者换变量。准确的说法是“按值传递引用值”。

### 对象相等与业务标识，先不要混为一谈

本课 Task 没有重写 equals，所以仍使用 Object 的默认身份相等行为。相同 ID 不会自动使两个对象的 equals 返回 true。

数据库阶段会讨论“一条业务记录可能在不同时间加载成不同 Java 实例”，再决定按什么语义比较。不急着给所有字段生成 equals/hashCode，尤其是对象字段还能变化时。

## 访问权限：公开操作，隐藏内部步骤

当前项目没有使用 Java 模块系统，可以先按包与类的边界理解这些修饰：

| 修饰方式 | 基本访问范围 | 本课用途 |
|---|---|---|
| private | 当前类的内部实现 | 字段、标题规范化辅助方法 |
| 不写修饰符 | 同一个包 | 最小教学版 TaskDraft 的成员 |
| public | 可供其他包的调用者使用 | Task 类、构造器、公开操作 |
| protected | 同包，或符合规则的子类访问 | 本课不用，继承时再展开 |

包访问权限不是写一个 `default` 关键字，也不会因为两个文件在同一个 Maven 项目，就自动认为它们同包。看的是 package 声明。

TaskDemo 与 Task 同在 `com.dailystudy.day005`，但依然不能绕过 Task 的 private 字段。封装边界是类，不是“熟悉的同事写的代码”。

::: details 顶层类与 protected 的补充边界
普通顶层类可以声明 public 或不写访问修饰符，不使用 private/protected；嵌套类有另一套可用修饰，当前先不展开。

跨包子类访问 protected 还有访问对象等限制，不是任何子类都可以通过任何父类变量访问。下一课用具体继承例子解释，先不要把它当成 public 的替代品。
:::

## 跑通正式版本：从创建到状态变化

正式代码只增加两个主源码文件：

```text
projects/java-foundations/src/main/java/com/dailystudy/day005/
├── Task.java       任务数据与允许的操作
└── TaskDemo.java   main，组织本课演示
```

从仓库根目录执行：

```bash
cd projects/java-foundations
mvn -B -ntp test
java -cp target/classes com.dailystudy.day005.TaskDemo
```

完整 Task 类如下。前面的片段都是帮助理解它的步骤，最后以这份源码为准：

<<< @/../projects/java-foundations/src/main/java/com/dailystudy/day005/Task.java

### toString：为什么直接打印对象也能看到字段

println 和字符串拼接需要文字表示，本例重写了 toString，返回便于观察状态的文本。`@Override` 表示这个方法覆盖继承而来的方法，编译器可帮助检查签名是否正确。

现在先知道所有普通 Java 类最终都继承 Object；继承机制下一课详细讲。如果不重写，默认文本通常类似类名加标识，不会自动以你希望的格式显示全部字段。

toString 不等于 JSON 序列化，也不应承担所有对外展示。实际用户对象有敏感字段时，更不能为了调试自动打印全部内容；本例仅包含教学任务信息。

### 完整演示程序与预期输出

<<< @/../projects/java-foundations/src/main/java/com/dailystudy/day005/TaskDemo.java

预期输出：

```text
初始任务一：Task{id=1, title='理解类与对象', completed=false}
初始任务二：Task{id=2, title='练习构造器', completed=false}
完成后：Task{id=1, title='理解封装', completed=true}
任务二仍未完成：true
改名被拒绝：已完成的任务不能修改标题，请先重新打开。
失败后标题：理解封装
重新打开后：Task{id=1, title='练习引用传递', completed=false}
别名指向同一对象：true
通过别名完成后任务二：Task{id=2, title='练习构造器', completed=true}
相同 ID 等于同一对象吗：false
指定初始状态：Task{id=3, title='已完成的示例任务', completed=true}
```

沿输出解释：创建 → 改名 → 完成 → 拒绝修改且保留原标题 → 重新打开 → 再改名 → 引用别名修改另一条任务。

这是实例状态与方法调用的演示，没有连接 HTTP、文件或数据库。运行结束后，任务不会自动保存；下一次启动又会按 main 创建新对象。不要把“有业务对象”与“已经持久化”混为一谈。

## 把类放回之前的请求链路

第 04 课讨论接口层、业务层与数据访问层。现在可以更具体地理解：

```text
HTTP 输入 → 接口层解析 → 业务操作调用 Task
                          ↓
                   Task 检查自身状态规则
                          ↓
                 数据访问层保存可持久化的数据
                          ↓
                    接口层返回响应
```

Task 负责自身的状态规则，但没有能力证明“调用者是谁、能操作谁的任务”。登录、工作空间权限仍由更高层结合身份与资源范围检查。

不要把 private 当成权限系统：它限制 Java 代码的成员访问，不是判断用户能否调用 HTTP 接口。也不要把一个对象的校验当成跨多条记录的数据库事务。

本课 Task 与实验服务的 Note 是不同模型；没有修改原来的 `/api/notes` 契约。后续先学对象之间的协作与集合，再接入数据库和 Spring Boot。

## 综合练习：自己改变规则，再验证变化

### A. 预测引用与状态

在 TaskDemo 的 main 内单独尝试：

```java
Task a = new Task(10, "练习引用");
Task b = a;
Task c = new Task(10, "练习引用");
b.complete();
```

a、b、c 的完成状态是什么？a == b、a == c 分别是什么？

::: details 展开参考解释
a 与 b 都看到 true，因为它们指向同一对象；c 仍为 false，是另一实例。a == b 为 true，a == c 为 false。相同 ID 不等于相同 Java 实例。
:::

### B. 让构造器拒绝无效对象

分别创建 ID 为 0、标题为 null、只有空格、121 个普通汉字的任务。用 try/catch 看异常；再用带首尾空白的合法标题确认规范化结果。

::: details 展开参考解释
前四种应抛 IllegalArgumentException，不能拿到一个正常构造完成的 Task。合法标题会保存去首尾空白后的文本。

例如 `new Task(1, "  合法任务  ").getTitle()` 返回“合法任务”。null 检查必须先于 strip，避免把可解释的输入错误变成 NullPointerException。
:::

### C. 确认失败没有破坏原值

先创建“原标题”，尝试改成空白；再完成任务，尝试改成“新标题”；最后 reopen 后正常改名。每一步打印标题和状态。

::: details 展开参考解释
第一次参数错误，仍是“原标题”、未完成；第二次状态错误，仍是“原标题”、已完成；重新打开后可改成“新标题”、未完成。

在赋值之前校验，失败路径与正常路径都能保持清楚的状态。这里只保护当前对象的操作，不自动提供多线程锁或数据库事务。
:::

### D. 补一个只读查询方法

新增 `public boolean canRename()`，表示当前状态是否允许改名。它不修改字段，也不接受参数。调用后状态应该改变吗？

::: details 展开参考写法
在 Task 中加入：

```java
public boolean canRename() {
    return !completed;
}
```

查询不会改变状态。rename 仍必须保留自己的状态检查，不能仅要求调用者先调用 canRename；否则绕过查询的调用者就能绕过规则。
:::

### E. 在不破坏封装的前提下改变产品规则

假设产品现在允许完成后的任务改名，但 title 规则保持不变。应该修改哪里？哪些测试也要同步调整？

::: details 展开参考解释
改 Task.rename 的状态限制，保留 normalizeTitle。不要让调用者直接写 private 字段，也不需要改所有页面去复制标题规则。

“已完成任务拒绝改名”的测试应变成“允许改名且状态仍已完成”；空白、超长标题被拒绝且保留旧值的测试继续成立。测试表达需求，需求改变时要重新核对，而不是为了绿色随意删断言。
:::

### F. 区分 final 与对象内容变化

`final Task task = new Task(1, "任务");` 后面，调用 complete 和把 task 赋为另一对象，哪个允许？把 Task.completed 改成 static 又会产生什么问题？

::: details 展开参考解释
调用 complete 允许，因为对象本身仍可变；给 task 重新赋引用不允许。completed 改为 static 会表达共享的一份完成状态，使独立任务的操作互相影响，与当前模型不符。
:::

### G. 继续完成一个小模型

参考 Task 写一个 Bookmark 类，先明确规则，再编码：正整数 ID；非空标题；URL 不能为空；未收藏与已收藏两个状态；提供 collect、uncollect、rename，不提供修改 ID 的方法。

不要复制任务“完成后不能改名”的规则，先决定书签业务是否需要它。URL 非空也不等于符合合法 HTTP/HTTPS 地址，若加入地址解析，要明确新的约束范围。

::: details 实现思路
先写字段与构造器，让对象完整创建；提取重复文本校验；给 ID final 和只读访问；通过操作方法修改状态；最后写一个 main，验证两实例隔离、无效输入和失败后保留原值。

当前能用自己掌握的语法完成。这个练习的目的不是扩张功能，而是把“数据、行为、规则和边界”独立组织一次。
:::

## 参考资料：按本课问题选读

讲解与示例由本仓库组织；知识顺序参考以下公开教程，所有代码以 Java 21 与本页实际源码为准。

- [Javaer：类与对象](https://github.com/itwanger/toBeBetterJavaer/blob/master/docs/src/oo/object-class.md)：先读字段、方法与实例，内部类、抽象类等后续再看。
- [Javaer：构造方法](https://github.com/itwanger/toBeBetterJavaer/blob/master/docs/src/oo/construct.md)：对照参数、重载与 this；区分无参构造器与编译器提供的默认构造器。
- [Javaer：访问权限](https://github.com/itwanger/toBeBetterJavaer/blob/master/docs/src/oo/access-control.md)：对照包与类的访问边界，protected 随继承专题学习。
- [Javaer：static](https://github.com/itwanger/toBeBetterJavaer/blob/master/docs/src/oo/static.md)：重点看共享状态与实例状态，不必先背 JVM 内存区域。
- [Java 官方：类与对象](https://dev.java/learn/classes-objects/)：按类声明、字段、构造器与方法选读。
- [Java 21 Object 文档](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html)：按需查 toString 与 equals，后续再展开 hashCode。

下一课继续**对象协作、继承与接口**：多个类如何分工，哪些行为应该由统一接口表达，什么时候组合比继承更合适。之后再学习集合，把一条任务扩展成多条任务管理。完整顺序见 [学习路线](/learning-path)。
