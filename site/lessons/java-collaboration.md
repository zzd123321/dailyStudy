---
description: 沿用任务模型生成两种清单，理解对象职责、构造器传入依赖、接口、多态、继承、super、protected、抽象类，以及组合的适用边界。
---

# 06 · 对象协作、继承与接口

第 05 课让 Task 管好自己的数据：标题是否合法、能不能改名、怎样完成和重新打开。现在增加一个要求：**把几条任务生成一份清单，既能显示中文状态，也能显示勾选符号。**

同一批数据，第一种展示是：

```text
任务清单
任务 #1 [待办] 理解接口
任务 #2 [完成] 练习对象协作
```

第二种展示是：

```text
任务清单
TASK #1 [ ] 理解接口
TASK #2 [x] 练习对象协作
```

这两种都是**纯文本输出**。这里没有生成网页、JSON 或正式的 Markdown 文档；那些格式各有自己的编码和转义规则。

本课从这个需求出发，依次学习类怎样分工、怎样通过接口合作、同一次调用为什么能有不同结果，最后再用继承复用两种格式中的共同步骤。沿用现有 Maven 项目，不添加环境安装说明。前面的短代码是讲解片段，完整源码和运行命令在本页后半部分。

## 先划分职责：任务不是清单，清单也不是任务

最容易想到的写法，是在 Task 中加入 printChinese、printChecklist，然后让 main 循环调用。任务很少、程序很小时可以这样写，但新增输出格式时，就得不断修改原本负责业务规则的类。

先问三个具体问题：

| 问题 | 谁来回答 |
|---|---|
| 一条任务的标题、状态是什么？允许怎样修改？ | Task：管理一条任务及其规则 |
| 一条任务应显示成什么文字？ | TaskFormatter 的实现：读取任务，生成一行文字 |
| 多条任务怎样排列，空清单怎样提示？ | TaskReport：遍历任务，把各行组合为清单 |

main 负责把这些对象创建好、连接起来并展示结果。它不应该再复制一遍标题校验，也不需要知道每种状态符号怎样拼接。

**对象协作，就是一个对象通过另一个对象公开的方法完成一部分工作。** TaskReport 调用格式化器，格式化器读取 Task；每个对象只承担这里明确分配给它的事情。

这不是“每个方法都必须单独建一个类”。当前确实有两种可替换格式，所以拆出格式化职责有实际用途。如果只有一段固定输出，先用一个普通方法也完全合理。

## 用接口表达：我需要你能做什么

TaskReport 不关心具体符号，只需要合作对象提供“把任务变成文字”的能力。先写出这个约定：

```java
public interface TaskFormatter {
    String format(Task task);
}
```

把它翻译成中文：**符合 TaskFormatter 约定的对象，可以接收一条 Task，返回一个 String。**

这里的方法声明没有 `{ ... }` 方法体，末尾是分号。它没有告诉你“怎么生成”，而是在说明“调用者可以怎样调用”。

### interface、implements 和方法体各负责什么

先看一个不用继承的简单实现，帮助理解接口本身：

```java
class SimpleTextFormatter implements TaskFormatter {
    @Override
    public String format(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("任务不能为空。");
        }
        String status = task.isCompleted() ? "完成" : "待办";
        String title = task.getTitle().replace('\r', ' ').replace('\n', ' ');
        return "任务 #" + task.getId() + " [" + status + "] " + title;
    }
}
```

- `interface` 定义约定。
- `implements TaskFormatter` 表示这个类明确实现该约定。
- `format` 的方法体给出具体做法。
- `@Override` 让编译器检查这里确实实现或重写了已有方法，方法名拼错时及时报错。

这个简单版用于理解过程。完整程序会让 TextFormatter 与 ChecklistFormatter 共享一个抽象父类，所以不会把 SimpleTextFormatter 也加入仓库。

接口中的 `String format(Task task);` 隐含 `public abstract`。实现时要写 public，不能缩小到包内可见或 private，否则调用者按照接口承诺就无法使用它。

### Java 的接口与 HTTP 接口、TypeScript interface 不同

HTTP 接口是客户端与服务器之间的通信入口，例如 `GET /tasks`。这里的 Java interface 是程序内部的类型约定：调用一个对象的方法，不会自动发送网络请求。

TypeScript 常根据对象的结构判断能否赋值；Java 使用这里声明的实现关系。一个类即使恰好有 `String format(Task task)` 方法，也不会因此自动成为 TaskFormatter，仍需要 implements，或继承已经实现它的父类。

```java
TaskFormatter formatter = new SimpleTextFormatter();
String line = formatter.format(new Task(1, "理解接口"));
System.out.println(line);
```

输出为 `任务 #1 [待办] 理解接口`。不能写 `new TaskFormatter()`：约定本身没有提供完整实现，不能直接实例化。

### 编译器检查签名，业务约定还需要人和测试检查

本课对 format 的完整要求是：接收有效任务，返回非 null 的单行文字，不修改任务；传入 null 时拒绝调用。

编译器能检查方法名、参数与返回类型等规则，却不会证明“这个方法一定不修改任务”。下面这种实现可能通过编译，但违背了我们的要求：

```java
public String format(Task task) {
    task.complete(); // 显示任务时顺便完成它，违背本课的格式化约定。
    return task.getTitle();
}
```

所以接口不是“写上就自动保证一切”。语义要靠清晰说明、代码审查和行为测试。本课的测试会检查两种内置格式没有改变任务状态和标题。

::: details 接口是不是只能有抽象方法？
不是。现代 Java 接口还支持 default 方法、static 方法，以及用于内部复用的 private 方法；接口字段隐含 public static final。它不能像普通类那样拥有每个实例自己的字段和构造器。

本课只需要一个抽象方法。先把最常见的“声明能力 → 实现能力 → 通过能力合作”学会，再在后面的集合、lambda 与框架中学习其他形式。不必为输出前缀建立常量接口。
:::

## 组合：让清单持有一个格式化器

TaskReport 不是 Task，也不是 TaskFormatter。它**使用**格式化器。因此它把这个合作对象保存为字段：

```java
public final class TaskReport {
    private final TaskFormatter formatter;

    public TaskReport(TaskFormatter formatter) {
        if (formatter == null) {
            throw new IllegalArgumentException("格式化器不能为空。");
        }
        this.formatter = formatter;
    }

    // render 的完整实现见后文。
}
```

字段类型是接口 TaskFormatter，实际传入的对象可以不同：

```java
TaskReport textReport = new TaskReport(new TextFormatter());
TaskReport checklistReport = new TaskReport(new ChecklistFormatter());
```

这就是这里使用的组合：一个对象持有并使用另一个对象。它不表示两个对象的生命周期一定绑定，也不表示传入时会自动复制合作对象。

### 构造器传入依赖：一个普通 Java 写法

TaskReport 要正常工作，依赖一个格式化器。创建它时从外部传入该对象，就叫**通过构造器注入依赖**。

你已经会写构造器、字段赋值和方法调用，所以已经能使用这个做法，不需要先学习 Spring。

对比下面两种选择：

```java
// 写死了合作对象：每个报告都只能使用这一种实现。
private final TaskFormatter formatter = new TextFormatter();

// 由创建者选择实现：报告只保存所需的能力。
private final TaskFormatter formatter;
```

第二种让 main 决定格式。以后测试也可以传入一个专门的实现，而不用修改报告类。

字段用 final，表示这份报告对象在构造后不能改为引用另一个格式化器。**final 没有让被引用的对象自动不可变**，这个含义与第 05 课相同。

类声明中的 final 是另一层限制：`final class TaskReport` 不允许别人继承这个类。它仍然可以创建很多实例。

## 遍历与委托：报告只负责组合结果

先观察 render 的核心部分：

```java
StringBuilder report = new StringBuilder("任务清单\n");
for (Task task : tasks) {
    report.append(formatter.format(task)).append('\n');
}
return report.toString();
```

这里有三个已经能拆解的动作：

1. 建立结果，先放入清单标题。
2. 按数组顺序拿到每条任务，委托 formatter 生成一行。
3. 追加这一行和换行符，最后返回整个字符串。

`for (Task task : tasks)` 是增强 for：每次循环把一个数组元素的引用值赋给局部变量 task。它不是创建任务副本，也没有修改数组的排列。

StringBuilder 是用来逐步拼接文字的对象。append 追加内容并返回当前 builder，所以可以连续调用；toString 才得到最后的 String。短小的一次拼接继续用 `+` 即可，这里用 builder 是为了表达循环中持续追加的过程。

`\n` 是字符串里的换行；`'\n'` 是一个 char。完整方法还会处理空数组和 null，稍后一起阅读。

### 怎样读取代码中的依赖关系

```text
main 创建并连接对象
  └─ TaskReport.render(Task[])
       └─ TaskFormatter.format(Task)
            └─ Task.getId / getTitle / isCompleted
```

报告不用自己读取 title，也没有复制 complete、rename 的规则。格式化器只查询数据；状态改变仍由 Task 自己处理。这条边界以后会继续用于“业务服务调用存储接口”，但本课没有文件保存或数据库操作。

## 多态：调用方式相同，实际实现不同

看这一句：

```java
TaskFormatter formatter = new TextFormatter();
```

左边 TaskFormatter 是变量的**声明类型**，右边 TextFormatter 是所创建对象的**实际类型**。

| 观察角度 | 决定什么 |
|---|---|
| 编译时看声明类型 | 这里允许调用哪些方法，参数是否匹配 |
| 运行时看实际对象 | 对可重写的实例方法，执行哪个具体实现 |

把符合约定的具体对象放进接口类型变量，是从具体类型到更一般类型的赋值，通常叫向上转型。这里不需要写强制转换，也没有产生第二个对象。

```java
Task task = new Task(1, "理解接口");
TaskFormatter first = new TextFormatter();
TaskFormatter second = new ChecklistFormatter();

System.out.println(first.format(task));
System.out.println(second.format(task));
```

结果分别是：

```text
任务 #1 [待办] 理解接口
TASK #1 [ ] 理解接口
```

同样调用 format，同样传入一条任务，实际对象不同，结果不同。这就是本例中的多态。完整程序中，两者共享父类的 format，其中调用的 statusLabel 会按实际子类执行不同实现。

### 为什么报告不需要按类型分支

报告的代码始终只写：

```java
formatter.format(task);
```

它不必写“如果是 TextFormatter，执行中文逻辑；如果是 ChecklistFormatter，执行符号逻辑”。新增实现时，只要符合接口约定，报告就能继续使用它。

这个价值来自分工，而不仅是“少写几个 if”。有时业务确实需要判断，不必为了使用接口消灭所有条件分支。

### 少用向下转型，先检查是否缺少公共能力

假设具体类额外定义了 preview 方法，声明为 TaskFormatter 的变量不能直接调用 preview，因为接口没有承诺这个能力。

可以强制转换到具体类，但对象若不是那个类，就会出现 ClassCastException。若调用者频繁猜测实现类型，通常应先检查：这个操作到底是不是所有实现都应提供？是否需要另一个接口？是否根本不属于报告的职责？

本例没有向下转型，也不需要使用 instanceof 来决定输出风格。

## 继承：共享同一套步骤，保留一个变化点

两种格式都有这些步骤：拒绝空任务、读取 ID、把标题内部的回车与换行替换为空格，再把前缀、状态和标题拼成一行。区别在于前缀和状态显示文字。

可以让两类各自 implements TaskFormatter，各写一份代码。也可以提取共享模板：

```java
abstract class StatusFormatter implements TaskFormatter {
    private final String itemPrefix;

    protected StatusFormatter(String itemPrefix) {
        if (itemPrefix == null || itemPrefix.isBlank()) {
            throw new IllegalArgumentException("条目前缀不能为空。");
        }
        this.itemPrefix = itemPrefix.strip();
    }

    @Override
    public final String format(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("任务不能为空。");
        }
        String singleLineTitle = task.getTitle().replace('\r', ' ').replace('\n', ' ');
        return itemPrefix + " #" + task.getId()
                + " [" + statusLabel(task.isCompleted()) + "] " + singleLineTitle;
    }

    protected abstract String statusLabel(boolean completed);
}
```

StatusFormatter 实现了接口的 format，却把“状态怎么显示”留给具体子类。不能单独创建它，因为它还不知道怎样完成 statusLabel。

### abstract 不等于所有方法都没有实现

抽象类可以同时具有字段、构造器、已实现的方法，以及没有方法体的抽象方法。

本例中：

- itemPrefix 是每个实例自己的字段。
- 构造器保证前缀有效。
- format 已经实现通用流程。
- statusLabel 是抽象方法，要求具体子类补齐。

**抽象类不能直接 new，但它的构造器仍会在创建子类对象时执行。** 如果一个类有抽象方法，它必须声明为 abstract；一个 abstract 类也可以没有抽象方法。

非抽象子类必须提供缺少的方法实现。若子类继续声明为 abstract，可以把未实现的方法继续留给下一层子类。

### extends 与 super：先初始化父类部分

中文状态的实现只需要补充变化点：

```java
final class TextFormatter extends StatusFormatter {
    TextFormatter() {
        super("任务");
    }

    @Override
    protected String statusLabel(boolean completed) {
        return completed ? "完成" : "待办";
    }
}
```

`extends StatusFormatter` 表达 TextFormatter 是一种 StatusFormatter。后者已经 implements TaskFormatter，所以 TextFormatter 也符合 TaskFormatter 类型，不需要重复写 implements。

创建 `new TextFormatter()` 时，按本课代码执行：

1. 进入 TextFormatter 构造器。
2. `super("任务")` 调用父类构造器，为这个对象初始化父类定义的前缀。
3. 父类构造器完成后，继续子类构造器后续内容；这里没有其他操作。

不是“另外创建了一个父对象”。一个子类实例包含父类定义的实例状态。父类的 private 字段仍存在，但子类不能直接访问它。

**构造器不会被继承。** 父类只有带参数构造器时，子类必须调用合适的父类构造器；不能假定总有隐式无参构造器。

本项目使用 Java 21。在这个版本中，显式的 `super(...)` 或 `this(...)` 必须是构造器第一条语句；没有显式调用时，编译器尝试插入 `super()`。父类没有可访问的无参构造器，就会编译失败。更高版本的新语法不在本课范围内。

### final format：通用步骤由父类维护

父类把 format 声明为 public final：调用者可以使用它，但子类不能重写它来绕开统一步骤。子类只实现 protected statusLabel。

当 `new ChecklistFormatter().format(task)` 执行时，先进入父类的 format；其中调用 statusLabel，再执行 ChecklistFormatter 的具体方法。

这说明继承与多态可以协作：父类保留整体步骤，子类完成变化部分。这个组织方式常被称为模板方法，但先理解实际调用顺序，比记住设计模式名称更有价值。

格式化不修改 Task。标题替换得到一个新的 String，只用于展示。比如任务标题为 `第一行\n第二行`，显示变成 `第一行 第二行`，原始标题仍保留换行。

如果未来 JSON 与 HTML 的整体格式完全不同，就没有必要强行继承这个模板，可以直接 implements TaskFormatter，或为不同输出重新定义适合的约定。

## 重写与重载：名字相近，解决的问题不同

第 05 课的 Task 构造器接收两组不同参数，是重载。本课的子类实现 statusLabel，是重写。

| 比较 | 重载 overload | 重写 override |
|---|---|---|
| 要做的事 | 同名操作接受不同参数 | 为已有实例方法提供具体实现 |
| 主要识别方式 | 参数数量或参数类型不同 | 相同方法名与参数列表，满足返回类型等规则 |
| 本课对应 | 可增加 render(Task)，与 render(Task[]) 同名 | TextFormatter 实现 statusLabel(boolean) |
| 选择依据 | 编译时根据可见候选方法与参数的声明类型选择 | 已选实例方法在运行时按实际对象分派 |

只改返回类型不能构成重载：两个 `format(Task)` 分别返回 String 和 int，会冲突。

重写时不能缩小访问范围。父类方法是 protected，子类可保持 protected 或扩大为 public，不能改成 private。引用类型的返回值可以使用符合规则的更具体子类型，称为协变返回类型；本课的返回值都保持 String。

### 加上 @Override，防止把重写误写成新方法

```java
@Override
protected String statusLabel(String completed) { // 参数错误，编译不通过。
    return completed;
}
```

父类要的是 boolean。如果删掉 @Override，这个 String 参数的方法也没有实现父类的抽象方法，具体子类仍然不完整。

static 方法属于类，不能像实例方法这样重写；同名静态方法涉及隐藏。private 方法也不能被子类重写。本课通过多态合作的都是实例方法，不把同名字段或 static 调用混在一起解释。

## protected：给扩展者开放，不是公开给所有人

在 StatusFormatter 中，format 是 public，statusLabel 与构造器是 protected，itemPrefix 是 private。它们面向不同对象：

| 成员 | 为什么这样开放 |
|---|---|
| public format | 普通调用者通过接口使用的能力 |
| protected statusLabel | 允许子类补充的变化点 |
| private itemPrefix | 父类自己维护的数据，子类也不能随意改 |

protected 不只是“子类可访问”：同一个包里的代码也可以访问。跨包时，访问受到继承关系及接收对象类型的约束。

::: details 跨包 protected 为什么不能随便通过父类对象访问？
下面以实例方法为例，片段分别属于两个包：

```java
// a/Base.java
package a;
public class Base {
    protected int value() { return 1; }
}
```

```java
// b/Child.java
package b;
import a.Base;

public class Child extends Base {
    int read(Child child, Base base) {
        int first = this.value();   // 允许。
        int second = child.value(); // 允许：接收者的声明类型是 Child。
        // int third = base.value(); // 不允许：接收者的声明类型是 Base。
        return first + second;
    }
}
```

跨包的子类内部，不是拿到任意 Base 引用就能访问受保护实例成员。这里接收者必须符合 Child 或其子类型的限制。即使 base 运行时实际指向 Child，只要表达式的声明类型仍是 Base，注释掉的那句也不能通过编译。

本例的两个格式化器与父类位于同包，主要用 protected 表达“这是扩展点”，没有把它当成严格的包内安全边界。protected 构造器的跨包规则另有细节，这里使用的是子类的 super 调用。
:::

## 选择接口、抽象类还是组合，先看关系

| 需要解决的问题 | 本课选择 | 原因 |
|---|---|---|
| 报告要使用可替换的格式化能力 | TaskFormatter 接口 | 报告只关心能力，不绑定某个实现 |
| 报告要调用格式化器 | 保存字段，通过构造器传入 | 报告使用格式化器，不是格式化器 |
| 两种状态格式共享步骤与前缀字段 | StatusFormatter 抽象类 | 共享实现，并要求子类补齐状态显示 |
| 一条任务要维护有效状态 | 原有 Task 类 | 状态修改继续由业务对象维护 |

接口主要表达能力；抽象类可以承载共享状态与实现。Java 类只能直接 extends 一个类，但可以 implements 多个接口，接口也可以 extends 多个接口。

### 能复用代码，不代表适合继承

`TaskReport extends Task` 不合理：一份清单不是一条任务。为了获得标题字段就继承 Task，会把任务的 complete、reopen 等能力也带给清单，含义变得混乱。

更难发现的问题，是子类表面上“像父类”，却改坏了父类的承诺。假设 Task 的 complete 代表完成任务，一个子类把 complete 改成什么也不做，那么使用 Task 的代码可能继续运行，却得到错误状态。编译器未必能发现这种业务违约。

因此决定继承前，除了问“是不是一种”，还要问：**把子类放到父类允许出现的地方，调用者原先依赖的行为还能成立吗？**

本例选择继承，是因为两种格式确实共享同一套流程。TaskReport 与格式化器之间用组合；不需要把所有类排成一条继承链。

### toString 与 Object：补上上一课留下的问题

Task 没有显式写 extends，也默认继承 Object。第 05 课的 `@Override public String toString()` 重写了 Object 提供的方法，用于生成便于观察的对象描述。

它不等于专门的报告格式：Task.toString 用于调试，TaskFormatter 负责这项清单需求。两者不用互相替代。

## 完整程序：三份源码，复用上一课的 Task

本课 Java 源码放在 `projects/java-foundations/src/main/java/com/dailystudy/day006/`。继续使用 `day005.Task`，无需复制或修改它。

为了让示例集中，CollaborationDemo.java 中包含一个 public 主类，以及三个包内可见的辅助类。这种文件结构在第 05 课已经用过：只有 public 顶层类要求与文件名一致。若以后其他包要直接使用这些具体类，再把它们拆成对应的 public 类文件。

### TaskFormatter.java：约定能力

<<< ../../projects/java-foundations/src/main/java/com/dailystudy/day006/TaskFormatter.java{java}

### TaskReport.java：验证输入，组合清单

<<< ../../projects/java-foundations/src/main/java/com/dailystudy/day006/TaskReport.java{java}

render 在调用格式化器前先检查整个数组，避免前几条已经交给合作对象处理，才发现后面包含 null。检查通过后才生成结果。

本例遵守格式化器的无副作用约定，但这个校验顺序也让调用行为更明确。它不是数据库事务，也不能保证并发线程在检查后不修改数组；线程安全会在后续专题学习。

### CollaborationDemo.java：连接对象与两种实现

<<< ../../projects/java-foundations/src/main/java/com/dailystudy/day006/CollaborationDemo.java{java}

先读 main，看到输入与输出入口；再读父类 format，最后读两个子类的 statusLabel。不必从文件第一行往下背。

### 运行与预期结果

在仓库根目录执行，使用前面课程已经配置好的 JDK 21 和 Maven：

```bash
mvn -B -ntp -f projects/java-foundations/pom.xml test
java -cp projects/java-foundations/target/classes com.dailystudy.day006.CollaborationDemo
```

输出为：

```text
【中文状态】
任务清单
任务 #1 [待办] 理解接口
任务 #2 [完成] 练习对象协作
【符号状态】
任务清单
TASK #1 [ ] 理解接口
TASK #2 [x] 练习对象协作
【完成第一条后，重新生成】
任务清单
TASK #1 [x] 理解接口
TASK #2 [x] 练习对象协作
改名被拒绝：已完成的任务不能修改标题，请先重新打开。
任务标题仍为：理解接口
```

没有 Maven 时，也可以直接编译已有源码。这两条命令同样在仓库根目录执行，UTF-8 编码保持中文输出一致：

```bash
javac -encoding UTF-8 -d projects/java-foundations/out projects/java-foundations/src/main/java/com/dailystudy/day005/Task.java projects/java-foundations/src/main/java/com/dailystudy/day006/*.java
java -cp projects/java-foundations/out com.dailystudy.day006.CollaborationDemo
```

### 完成任务后，为何同一个报告对象会显示新状态

TaskReport 保存格式化器，不保存上一次生成的结果。每次 render 都重新遍历传入的数组，再查询任务当前状态。

`first.complete()` 改变 first 指向对象的状态；tasks[0] 保存着同一个对象的引用，所以重新生成时读到已完成。无需重新创建数组，也无需重新创建报告。

但之前已经返回的 String 不会自动更新。它是一份当时生成的文字结果：

```java
String before = textReport.render(tasks);
first.complete();
String after = textReport.render(tasks);
```

如果 first 原来未完成，before 仍含“待办”，after 含“完成”。这与前端响应式系统不同：这里没有订阅，也没有自动重新渲染机制。

## 把正常输入和边界输入放在一起理解

| 调用 | 本例结果 | 原因 |
|---|---|---|
| render 有效任务数组 | 按数组顺序生成清单 | 顺序由报告控制 |
| render(new Task[0]) | 标题加“（暂无任务）”，不调用格式化器 | 空集合是有效输入 |
| render(null) | IllegalArgumentException | 缺少任务数组 |
| 数组中包含 null | 调用格式化器前拒绝 | 有数组，不代表每个元素都有效 |
| new TaskReport(null) | 构造时拒绝 | 缺少必要合作对象 |
| 内置格式化器 format(null) | 拒绝 | 单条调用也要保护边界 |
| 标题内部含换行 | 展示替换为空格，原始标题不变 | 显示规则不修改业务数据 |
| 已完成任务 rename | 沿用 Task 的状态限制 | 输出方式没有更改业务规则 |

错误入口不同，但判断方法相同：哪个对象负责这个规则？失败前有没有修改状态或调用其他对象？调用者会看到怎样的结果？

报告类信任符合约定的格式化器。它没有自动拦截任意恶意实现，也不检查返回字符串是否违反单行约定；本课测试检查我们提供的实现。生产系统若接收不受信任的扩展，还需要明确另外的验证和隔离边界。

## 独立练习：先预测，再修改，再运行

### A. 分清声明类型与实际类型

`TaskFormatter formatter = new ChecklistFormatter();` 中哪个是声明类型，哪个是实际类型？TaskReport 为什么不知道 ChecklistFormatter 的名字也能调用 format？

::: details 展开参考解释
声明类型是 TaskFormatter，实际类型是 ChecklistFormatter。报告编译时依据接口检查调用；实际对象通过继承实现了该接口，运行时使用父类 format，其中的 statusLabel 分派到 ChecklistFormatter。

这里仅创建一个格式化器对象，赋给接口类型变量没有复制对象。
:::

### B. 新增一种输出，不修改报告

新增 EmojiFormatter，前缀为“任务”，未完成显示 `○`，完成显示 `✓`。报告的排列与空数组规则保持一致。新增类放在同包，main 中再创建一份报告验证。

::: details 展开参考代码
可放在 CollaborationDemo.java 中，与其他辅助类并列：

```java
final class EmojiFormatter extends StatusFormatter {
    EmojiFormatter() {
        super("任务");
    }

    @Override
    protected String statusLabel(boolean completed) {
        return completed ? "✓" : "○";
    }
}
```

main 增加 `new TaskReport(new EmojiFormatter()).render(tasks)` 的打印。任务 1 未完成时输出 `任务 #1 [○] 理解接口`，完成后对应符号为 ✓。Task 与 TaskReport 都不用修改。
:::

### C. 不通过父类，直接实现接口

写 IdFormatter，只显示 `ID=数字`。它需要 itemPrefix 或 statusLabel 吗？通过它生成报告，验证仍按输入顺序显示。

::: details 展开参考代码
```java
final class IdFormatter implements TaskFormatter {
    @Override
    public String format(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("任务不能为空。");
        }
        return "ID=" + task.getId();
    }
}
```

不需要继承 StatusFormatter，因为不使用那套模板。TaskReport 接受接口，既能使用继承共享实现的格式化器，也能使用独立实现。若输入 ID 依次为 5、1，清单的两行仍依次为 `ID=5`、`ID=1`。
:::

### D. 给单条输入一个方便入口

增加 `render(Task task)`，委托原来的数组方法处理。这是重载还是重写？如果调用 `report.render(null)`，为什么会产生新的编译问题？

::: details 展开参考代码与解释
在 TaskReport 内加入：

```java
public String render(Task task) {
    return render(new Task[]{task});
}
```

这是重载：同名方法，参数类型不同。有效任务会复用原逻辑；null 任务进入数组后仍会被拒绝。

但是 render(Task) 和 render(Task[]) 的参数都是引用类型，二者没有子类型关系，裸 null 可以匹配两者，编译器无法选出唯一方法。测试空引用时可写 `render((Task[]) null)` 明确数组类型，也可以避免增加容易混淆的重载，使用不同名称。这是修改 API 时需要考虑的调用体验。
:::

### E. 比较已有文字和重新生成的文字

创建一条未完成任务，先 render 保存为 before，再 complete，重新 render 为 after。打印两者，并解释为什么 before 不变。然后 reopen 再 render。

::: details 展开参考解释
before 是第一次生成的 String，不是对象状态的实时视图。after 是重新读取完成状态得到的文字。reopen 后再生成，会再次显示待办。

TaskReport 没有缓存结果，数组引用指向原来的 Task，任务对象也没有被格式化器复制。不要把“持有同一个可变对象”和“字符串结果自动更新”混为一谈。
:::

### F. 预测三种编译失败

分别尝试下面改动，不要同时改。解释每种失败，再恢复源码：把 TextFormatter.statusLabel 改为 private；给 TextFormatter 增加自己的 format(Task)；删掉 TextFormatter 构造器中的 super 调用。

::: details 展开参考解释
第一种缩小了 protected 的可见性，不符合重写规则。

第二种试图重写父类的 final format，不允许。

第三种会尝试隐式调用 super()，但 StatusFormatter 只有接收 String 的构造器，没有无参构造器。构造器不会自动从父类继承下来。
:::

### G. 验证展示规则不改变原数据

用标题 `第一行\n第二行` 创建任务。调用两种内置格式化器，检查输出是一行、Task.getTitle 仍包含原来的换行，再检查 completed 也没有变化。

::: details 展开参考写法
在 main 中加入：

```java
Task task = new Task(3, "第一行\n第二行");
String line = new TextFormatter().format(task);
System.out.println(line);
System.out.println("输出含换行：" + line.contains("\n"));
System.out.println("原标题含换行：" + task.getTitle().contains("\n"));
System.out.println("任务完成状态：" + task.isCompleted());
```

输出依次是 `任务 #3 [待办] 第一行 第二行`、false、true、false。展示用的是替换后得到的新字符串，没有调用 rename，也没有改变对象状态。
:::

### H. 判断一个设计是否符合关系

有人为了复用 Task 的标题字段，建议让 TaskReport extends Task；另一个人建议让报告持有格式化器。分别说出关系、可能带来的方法，以及你会选择哪一种。

::: details 展开参考解释
报告不是一条任务，继承会让它获得 complete、reopen 等不属于报告的操作，还会被当成任务传入其他逻辑。仅仅需要“有个标题”，不足以证明它是 Task。

报告需要使用格式化能力，持有 TaskFormatter 更准确。若清单以后有自己的标题，可以在报告中声明该字段，并维护报告自己的规则。
:::

## 本课学会的能力，会接到哪里

现在你应能从输入、职责和调用顺序解释这个程序：main 选择实现，TaskReport 组合输出，TaskFormatter 约定能力，StatusFormatter 共享步骤，具体子类提供状态文字，而 Task 继续管理业务状态。

下一课学习**集合与泛型**：目前数组长度固定，新增和删除任务不方便；用 List 管理任务，用 Map 按 ID 查找，再理解泛型为什么能减少类型错误。之后才把异常处理、文件保存与测试串入任务管理器。

## 参考资料：按问题选读

本文按当前项目重新组织知识与示例，没有复制外部教程全文。中文教程适合补充不同例子，官方文档用于核对语言规则；阅读时区分现代 Java 接口与旧版本限制。

- [Javaer：Java 接口](https://github.com/itwanger/toBeBetterJavaer/blob/master/docs/src/oo/interface.md)：重点看定义、实现、接口类型变量；default 等扩展形式暂时选读。
- [Javaer：Java 抽象类](https://github.com/itwanger/toBeBetterJavaer/blob/master/docs/src/oo/abstract.md)：对照抽象方法、普通方法和子类补齐行为。
- [Javaer：封装、继承和多态](https://github.com/itwanger/toBeBetterJavaer/blob/master/docs/src/oo/encapsulation-inheritance-polymorphism.md)：用本课的声明类型、实际类型和组合关系核对例子。
- [Javaer：重写与重载](https://github.com/itwanger/toBeBetterJavaer/blob/master/docs/src/basic-extra-meal/override-overload.md)：先看参数差异与子类重写，字节码分析以后再读。
- [Java 官方：接口](https://dev.java/learn/interfaces/)：核对 implements 与接口方法的规则。
- [Java 官方：继承](https://dev.java/learn/inheritance/)：核对 super、重写、抽象类与访问限制。
- [Java 21 StringBuilder API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/StringBuilder.html)：按需查看 append 与 toString，无需通读所有方法。

需要复习对象与引用时，回到 [第 05 课](/lessons/java-objects)；学习顺序与后面的项目见 [学习路线](/learning-path)。
