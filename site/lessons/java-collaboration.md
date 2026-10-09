---
description: 用完整小程序理解继承、super、方法重写、多态与接口，再学习抽象类，并在任务清单中应用组合。
---

# Java 04 · 继承、接口与多态

类可以单独使用，也可以通过继承表达一般类型与具体类型的关系。本章先用两种员工的工作行为解释继承与多态，再用消息输出解释接口。最后把这些写法放回任务清单。

主要参考 [MOOC：继承](https://github.com/rage/java-programming/blob/master/data/part-9/1-inheritance.md)与[接口](https://github.com/rage/java-programming/blob/master/data/part-9/2-interfaces.md)。先看完整的小程序，再读规则；暂时不讨论框架容器或复杂设计模式。

## 继承

开发者也是员工，具有姓名，但工作内容可以不同。保存下面完整程序为 `InheritanceDemo.java`：

```java
class Employee {
    private final String name;

    Employee(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String work() {
        return "处理一般事务";
    }
}

class Developer extends Employee {
    Developer(String name) {
        super(name);
    }

    @Override
    public String work() {
        return "编写程序";
    }
}

public class InheritanceDemo {
    public static void main(String[] args) {
        Employee first = new Employee("小陈");
        Employee second = new Developer("小林");
        System.out.println(first.getName() + "：" + first.work());
        System.out.println(second.getName() + "：" + second.work());
    }
}
```

运行：

```bash
javac -encoding UTF-8 InheritanceDemo.java
java InheritanceDemo
```

输出：

```text
小陈：处理一般事务
小林：编写程序
```

extends 表示继承。Developer 可以使用 Employee 公开的 getName，且通过自己的 work 提供具体行为。Java 类只能直接继承一个类。

### 父类的 private 字段

Developer 对象包含 Employee 定义的 name 状态，但不能直接访问 private name。使用 getName 读取即可。

不要为了省略一个 getter，把所有父类字段都改成 protected。父类数据仍可以封装，继承不要求开放内部字段。

### super 与构造器

`super(name)` 调用父类构造器，初始化当前对象的父类部分。它不是创建另一个 Employee 对象。

构造器不被继承。Employee 只有一个带参数构造器，所以 Developer 必须选择合适的父类构造器。删掉 super(name) 时，Java 21 会尝试隐式 super()，但这里没有可用的无参父类构造器。

显式 super(...) 在 Java 21 中必须是构造器第一条语句。父类构造完成后，再继续子类构造器的其他语句。

## 方法重写

Developer 的 work 与 Employee 的 work 使用相同名字、相同参数列表，提供了新的实现，这叫重写。

@Override 用于编译检查。误写成 works 时，注解能帮助发现它没有重写原方法。

重写还需要遵守这些规则：

- 不能缩小可见性：父类 public，子类不能改为 private。
- 返回类型要相同或满足协变返回规则；本例都是 String。
- final 方法不能被重写。
- private 方法不作为子类可重写的方法，static 方法也不是这里的实例方法分派。

### 调用父类实现

可以在新行为中保留父类的一部分：

```java
@Override
public String work() {
    return super.work() + "，然后编写程序";
}
```

super.work 明确使用父类实现。this.work 则会再次调用当前对象的方法，放在这个方法内会形成递归，不能用来替代 super.work。

## 多态

```java
Employee second = new Developer("小林");
```

变量的声明类型是 Employee，对象的实际类型是 Developer。

编译时根据 Employee 检查允许调用的成员；运行时，对可以重写的实例方法，根据实际对象选择实现。因此 second.work 执行 Developer.work。

这叫多态：代码通过一般类型调用同一个方法，不同实际对象可以给出不同实现。

### 方法参数也可以使用父类类型

```java
static void printWork(Employee employee) {
    System.out.println(employee.getName() + "：" + employee.work());
}
```

它既可以接收 Employee，也可以接收 Developer。方法不需要判断“到底是哪一种员工”。给它新增 Tester 子类时，只要仍然满足 Employee 的行为约定，它就能继续使用。

如果 Developer 有额外的 debug 方法，Employee 类型变量不能直接调用它，因为 Employee 没有声明这个能力。强制向下转型只有在对象类型匹配时才成立，否则会产生 ClassCastException。当前例子不需要转型。

## 重载与重写

```java
void printWork(Employee employee) { /* ... */ }
void printWork(Employee employee, String prefix) { /* ... */ }
```

同名、参数列表不同叫重载，编译时根据参数等信息选择方法。Developer 提供同签名的 work 是重写，实例调用根据实际对象分派。

| 项目 | 重载 | 重写 |
|---|---|---|
| 主要变化 | 参数数量或类型 | 同签名实例方法的实现 |
| 本章例子 | 两个 printWork | Developer.work |
| 选择发生 | 编译时选择方法签名 | 运行时分派实例实现 |

只改返回类型不能形成重载。字段和 static 方法也不要套用这里的实例多态规则。

## 抽象类

如果一般员工没有统一的工作实现，可以要求每个具体子类提供它：

```java
abstract class Worker {
    private final String name;

    Worker(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public abstract String work();
}

class Tester extends Worker {
    Tester(String name) {
        super(name);
    }

    @Override
    public String work() {
        return "执行测试";
    }
}
```

Worker 有字段、构造器和已实现的 getName，但 work 没有方法体。存在抽象方法的类必须是 abstract，不能直接 new Worker。

可以创建 `Worker worker = new Tester("小周")`。创建子类时仍会执行 Worker 构造器。非抽象子类必须补齐尚未实现的抽象方法。

抽象类主要用于这里的共享状态与实现，不是每个普通类都需要配一个抽象父类。

## 接口

不同对象未必有共同父类，却可能提供同一能力。比如控制台输出和带前缀输出，都可以“输出一条消息”。

保存下面完整程序为 `InterfaceDemo.java`：

```java
interface MessageOutput {
    void send(String message);
}

class ConsoleOutput implements MessageOutput {
    @Override
    public void send(String message) {
        System.out.println(message);
    }
}

class PrefixedOutput implements MessageOutput {
    private final String prefix;

    PrefixedOutput(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public void send(String message) {
        System.out.println(prefix + message);
    }
}

public class InterfaceDemo {
    static void announce(MessageOutput output) {
        output.send("任务已完成");
    }

    public static void main(String[] args) {
        announce(new ConsoleOutput());
        announce(new PrefixedOutput("[提示] "));
    }
}
```

编译运行：

```bash
javac -encoding UTF-8 InterfaceDemo.java
java InterfaceDemo
```

输出：

```text
任务已完成
[提示] 任务已完成
```

interface 声明能力，implements 表示类明确实现这个接口。announce 只要求 MessageOutput，不关心具体类名。

这里普通接口方法隐含 public abstract，所以实现必须是 public。接口不能直接实例化，也没有实例字段或构造器。现代接口还支持 default、static 和 private 方法，后续遇到对应需求再学习。

Java interface 与 HTTP 接口不是同一个概念。前者是进程内类型约定，后者是网络通信入口。另外，Java 不会仅因为一个类恰好有 send 方法，就自动把它当作 MessageOutput；必须声明相应实现关系。这一点不同于 TypeScript 常用的结构类型检查。

## 组合与依赖传入

一个对象需要使用输出能力，可以把接口作为字段：

```java
class TaskNotice {
    private final MessageOutput output;

    TaskNotice(MessageOutput output) {
        if (output == null) throw new IllegalArgumentException("输出对象不能为空。");
        this.output = output;
    }

    void completed(String title) {
        output.send("已完成：" + title);
    }
}
```

TaskNotice 使用输出对象，它不是输出对象，因此不需要继承 ConsoleOutput。构造器从外部接收依赖，这就是构造器注入依赖的普通 Java 写法，和 Spring 是否存在无关。

创建时可以选择不同实现：

```java
TaskNotice notice = new TaskNotice(new PrefixedOutput("[任务] "));
notice.completed("阅读教材");
```

输出 `[任务] 已完成：阅读教材`。这段只演示输出协作，不负责修改 Task 状态。

## 在任务清单中应用

仓库已有完整示例，仍在历史路径 `projects/java-foundations/src/main/java/com/dailystudy/day006/`。

| 类型 | 负责的内容 |
|---|---|
| Task | 任务数据与修改规则 |
| TaskFormatter | 把任务转成单行纯文本的约定 |
| TaskReport | 按数组顺序组合文字 |
| TextFormatter / ChecklistFormatter | 两种状态显示 |

接口如下：

<<< ../../projects/java-foundations/src/main/java/com/dailystudy/day006/TaskFormatter.java{java}

报告通过构造器接收接口实现：

<<< ../../projects/java-foundations/src/main/java/com/dailystudy/day006/TaskReport.java{java}

先检查数组和元素，再调用 formatter。空数组是有效输入，显示“暂无任务”；null 引用或 null 元素则拒绝。这里没有数据库事务，也没有并发保证。

从仓库根目录运行：

```bash
mvn -B -ntp -f projects/java-foundations/pom.xml test
java -cp projects/java-foundations/target/classes com.dailystudy.day006.CollaborationDemo
```

同一组任务的两种输出分别包含：

```text
任务 #1 [待办] 理解接口
任务 #2 [完成] 练习对象协作
```

```text
TASK #1 [ ] 理解接口
TASK #2 [x] 练习对象协作
```

TaskReport 保存格式化器，不保存结果。每次 render 读取任务当前状态；已经返回的 String 则保留当时的文字，不会自动更新。

::: details 选读：共享模板、protected 与 final
完整实现如下：

<<< ../../projects/java-foundations/src/main/java/com/dailystudy/day006/CollaborationDemo.java{java}

StatusFormatter 共享空值检查、读取 ID 和处理标题的步骤，子类只补充 statusLabel。format 声明为 final，子类不能重写它绕过统一流程。TextFormatter 通过 super 设置前缀，在 statusLabel 中提供中文状态。

protected 方法同包可访问，跨包时可供子类按继承规则访问。以实例成员为例：跨包子类内部可以通过 this 或子类类型的接收者访问它，不能通过任意父类类型引用直接访问。protected 不是“所有地方拿到父类对象都能用”。

这种共享模板适用于两种格式的整体结构相同。如果新格式完全不同，可以直接 implements TaskFormatter，不必强行继承 StatusFormatter。
:::

## 继承的适用范围

继承表达子类可以作为父类使用。除了名字上的“是一种”，还要检查行为：把子类放在父类的位置，调用者原来依赖的规则是否仍成立？

TaskReport 不是一条 Task。如果仅为复用标题字段而继承 Task，就会得到 complete、reopen 等不属于报告的操作。报告有自己的标题时，定义自己的字段更准确。

接口也只提供编译器能检查的类型约定。TaskFormatter 的“返回单行且不修改任务”属于行为要求，仍需文档和测试。一个实现若在 format 中调用 task.complete，可能通过编译，但已经违背本例约定。

## 练习

### 1. 增加一个子类

为 Employee 增加 Tester，work 返回“执行测试”。使用 Employee 类型变量调用它，再传给 printWork。

::: details 参考写法
```java
class Tester extends Employee {
    Tester(String name) {
        super(name);
    }

    @Override
    public String work() {
        return "执行测试";
    }
}
```

这属于最开始的 Employee 示例，和抽象类片段分开练习，以免同名 Tester 重复声明。通过父类变量调用时仍使用 Tester.work。
:::

### 2. 构造器

从 Developer 中删掉 super(name)，再编译。为什么即使父类已经有构造器，子类仍会失败？

::: details 答案
构造器不被继承。隐式调用的是 super()，不是自动把 name 传给父类。Employee 没有无参构造器。
:::

### 3. 独立实现接口

写 SilentOutput，实现 send 但不打印。announce 是否需要修改？

::: details 答案
不需要。announce 依赖 MessageOutput。SilentOutput 需要声明 implements 并提供 public void send(String message)；方法体可以为空，这里将能力约定为接受消息，没有规定必须打印。
:::

### 4. 新增任务格式

写 IdFormatter，直接 implements TaskFormatter，只返回 `ID=数字`。将它传给 TaskReport。

::: details 参考写法
放在 day006 同包中，使用现有 Task：

```java
class IdFormatter implements TaskFormatter {
    @Override
    public String format(Task task) {
        if (task == null) throw new IllegalArgumentException("任务不能为空。");
        return "ID=" + task.getId();
    }
}
```

使用 `new TaskReport(new IdFormatter())`，报告与 Task 均不用修改。输出不使用共享状态模板，因而直接实现接口更合适。
:::

### 5. 访问权限

把接口实现方法改成 private，把父类的 work 改成 final 后仍在子类重写。分别会产生什么结果？

::: details 答案
两者都不能编译。前者缩小接口承诺的 public 可见性；后者试图重写禁止重写的 final 方法。
:::

## 对应阅读

- [MOOC：Class inheritance](https://github.com/rage/java-programming/blob/master/data/part-9/1-inheritance.md)：构造器、父类调用和运行时方法选择。
- [MOOC：Interfaces](https://github.com/rage/java-programming/blob/master/data/part-9/2-interfaces.md)：接口类型作为变量、参数和返回值。
- [Javaer：继承与多态](https://github.com/itwanger/toBeBetterJavaer/blob/master/docs/src/oo/encapsulation-inheritance-polymorphism.md)、[接口](https://github.com/itwanger/toBeBetterJavaer/blob/master/docs/src/oo/interface.md)：补充中文解释。

下一章学习集合：用 List 管理多条任务，用 Map 根据 ID 查询。完整顺序见[学习路线](/learning-path)。
