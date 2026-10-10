---
description: 从集合的类型检查理解泛型、包装类型和通配符，学习 equals 与 hashCode，并用稳定的任务键完成去重和查询。
---

# Java 06 · 泛型、包装类型与对象相等

上一章的任务管理器使用 `Map<Integer, Task>`：键是整数，值是任务。本章先解释尖括号中的类型有什么作用，再讨论 HashSet 怎样判断重复、HashMap 怎样认出一个键。

需要用到前面学过的引用、接口、方法重写和集合。先读普通泛型与包装类型，再读对象相等，最后运行任务键的示例。通配符与类型擦除放在折叠内容中，可以在完成基本练习后补读。

## 泛型与类型检查

### 带类型的集合

保存为 `TypedListDemo.java`，编译运行：

```java
import java.util.ArrayList;
import java.util.List;

public class TypedListDemo {
    public static void main(String[] args) {
        List<String> titles = new ArrayList<>();
        titles.add("阅读泛型教材");
        titles.add("练习对象相等");
        String first = titles.get(0);
        System.out.println(first.length());
        // titles.add(100); // 取消注释后无法编译
    }
}
```

```bash
javac -encoding UTF-8 TypedListDemo.java
java TypedListDemo
```

输出 `6`。`List<String>` 把元素类型约定为 String。编译器据此检查 add 的参数，并知道 get 返回 String，所以这里不需要强制转换。

`new ArrayList<>()` 中的空尖括号称为 diamond，编译器根据左侧声明推断 String。它不是说“任何类型都可以”。`List<Task>` 同理可以保存 Task 及其子类对象，不能保存一个任务标题字符串。部分集合允许 null；指定类型并不会自动排除空值。

相比之下，省略类型参数的 `List` 是**原始类型**（raw type）：

```java
import java.util.ArrayList;
import java.util.List;

public class RawListDemo {
    public static void main(String[] args) {
        List values = new ArrayList();
        values.add("阅读教材");
        values.add(100);
        String title = (String) values.get(1);
        System.out.println(title);
    }
}
```

这是一个故意写错的程序。`javac -Xlint:rawtypes,unchecked RawListDemo.java` 会给出原始类型和未检查操作的警告；它仍可能生成 class 文件。运行时，把 Integer 强制转换为 String 会产生 ClassCastException。

使用泛型，把这类错误尽量提前到编译时发现。不要通过删除尖括号或忽略警告来让代码“通过”。需要允许多种对象时可以明确写 `List<Object>`；取出后得到的是 Object，调用具体类型的方法前仍要检查类型。

### 泛型类

泛型不只用于集合。下面的 Box 保存一个值，调用方决定这个值的类型。保存为 `GenericIntro.java`：

```java
import java.util.List;

public class GenericIntro {
    public static void main(String[] args) {
        Box<String> title = new Box<>("阅读教材");
        Box<Integer> count = new Box<>(2);
        title.set("练习泛型");
        String text = title.get();
        int number = count.get();
        System.out.println(text + "：" + number);
        System.out.println(first(List.of("任务一", "任务二")));
        System.out.println(first(List.of(10, 20)));
    }

    static <T> T first(List<T> values) {
        if (values.isEmpty()) {
            throw new IllegalArgumentException("列表不能为空。");
        }
        return values.get(0);
    }
}

class Box<T> {
    private T value;

    Box(T value) {
        this.value = value;
    }

    T get() {
        return value;
    }

    void set(T value) {
        this.value = value;
    }
}
```

输出：

```text
练习泛型：2
任务一
10
```

同一文件里可以有多个顶层类，只有 GenericIntro 是 public，文件名与它一致。Box 是这个例子的辅助类，后续需要跨包使用时再单独放进 `Box.java` 并声明 public。

`class Box<T>` 声明类型参数 T；在 Box 的字段、参数和返回值中都能使用它。对于 `Box<String>`，set 接收 String，get 返回 String；对于 `Box<Integer>`，对应的是 Integer。不能向前一个 Box 写入整数。

T 是类型参数的名字，不是新的业务类。常见名字还有 E（元素）、K（键）、V（值）；`Map<K, V>` 的两个参数可以不同。编译器检查的是实际的类型关系，不是字母的含义。

`Box<Object>` 与 `Box<String>` 也不同：前者可以接收 String、Integer 等对象，但 get 的声明返回类型是 Object；后者把存入和取出的类型都限制为 String。这正是泛型比把所有字段都声明为 Object 更方便的地方。

### 泛型方法

刚才的 first 是泛型方法：

```java
static <T> T first(List<T> values)
```

从左到右读：static 方法 → 声明一个类型参数 T → 返回 T → 参数是元素类型为 T 的 List。调用时通常不用手写 T，编译器会推断：

```java
String name = first(List.of("甲", "乙"));
Integer id = first(List.of(1, 2));
```

这里方法的 T 与 Box 的 T 各自独立。普通类也能声明泛型方法，泛型类也可以拥有自己的静态泛型方法。

first 明确拒绝空列表，避免把“没有第一个元素”悄悄当成 null。它假定传入的是非 null 列表；完整的输入校验和异常处理放在下一章。

参考：[MOOC：Type parameters](https://github.com/rage/java-programming/blob/master/data/part-12/1-type-parameters.md) 从自定义容器讲起；[Javaer：泛型](https://github.com/itwanger/toBeBetterJavaer/blob/master/docs/src/basic-extra-meal/generic.md) 可补充泛型类、接口与方法的不同写法。

::: details 补充：整数列表与数值列表的类型关系
Integer 是 Number 的子类，但 `List<Integer>` 不是 `List<Number>` 的子类型。假设允许下面的赋值：

```java
List<Integer> integers = new ArrayList<>();
// List<Number> numbers = integers; // 实际上不能编译
// numbers.add(1.5);               // 如果允许，就把 Double 写进整数列表
```

两个变量会指向同一个列表。原本只能装 Integer 的列表出现了 Double，类型约定就被破坏了。这种泛型类型关系通常称为“不变”。

如果一个方法只需要读取数值，可以使用上界通配符。下面是完整程序 `NumbersDemo.java`：

```java
import java.util.ArrayList;
import java.util.List;

public class NumbersDemo {
    public static void main(String[] args) {
        System.out.println(sum(List.of(1, 2, 3)));
        System.out.println(sum(List.of(1.5, 2.5)));
        List<Number> result = new ArrayList<>();
        addSample(result);
        System.out.println(result);
    }

    static double sum(List<? extends Number> values) {
        double total = 0;
        for (Number value : values) total += value.doubleValue();
        return total;
    }

    static void addSample(List<? super Integer> target) {
        target.add(1);
        target.add(2);
    }
}
```

输出是 `6.0`、`4.0`、`[1, 2]`，各占一行。sum 的参数表示：元素是某个 Number 子类型，但方法不知道究竟是 Integer 还是 Double。它可以把元素读成 Number，却不能安全地 add 一个非 null 数值，因为可能写错实际元素类型。

addSample 的参数表示：元素类型是 Integer 或它的某个父类型。无论实际是 `List<Integer>`、`List<Number>` 还是 `List<Object>`，写入 Integer 都符合约定；但读取时只能保证得到 Object。

| 声明 | 安全读取的类型 | 能写入的非 null 元素 |
|---|---|---|
| `List<T>` | T | T 或其子类型 |
| `List<?>` | Object | 不能直接写入 |
| `List<? extends Number>` | Number | 不能直接写入 |
| `List<? super Integer>` | Object | Integer |

上表讨论编译时的类型限制。通配符不等于不可修改：例如 clear 不依赖元素类型，但 List.of 创建的列表会因禁止结构修改而拒绝 clear。允许写入 null 的类型声明也不保证具体集合接受 null。

PECS 是取元素用 extends、放元素用 super 的记忆法。先根据方法需要怎样读写来选参数，再记缩写即可。sum 还假定每个元素非 null；null 调用 doubleValue 会失败。

继续阅读：[JavaGuide：通配符](https://github.com/Snailclimb/JavaGuide/blob/main/docs/java/basis/generics-and-wildcards.md#通配符)。
:::

::: details 补充：类型擦除与常见限制
Java 的普通泛型主要由编译器检查。编译后，无上界的 T 通常擦除为 Object，有上界的 T 擦除为其上界；编译器会在需要的位置插入类型转换等代码。类文件仍可以保留泛型声明信息，后续反射专题再讨论。

`ArrayList<String>` 与 `ArrayList<Integer>` 不会因此变成两个不同的运行时类。由此可以理解一些限制：

- 类型参数不能写基本类型：使用 `List<Integer>`，不能使用 `List<int>`。
- 不能直接 `new T()`，也不能 `new T[10]`：运行时并不知道 T 对应哪个具体类。
- 不能用 `instanceof List<String>` 检查元素类型。`instanceof List<?>` 只能判断对象是不是列表。
- 不能仅凭 `List<String>` 与 `List<Integer>` 重载同名方法：擦除后的参数都是 List，会发生冲突。

原始类型之所以危险，是因为它能绕过部分编译检查；集合不会在每次 add 时自动扫描对象是否符合 String 的约定。正常代码不要使用原始类型或未检查的强制转换。

继续阅读：[JavaGuide：类型擦除](https://github.com/Snailclimb/JavaGuide/blob/main/docs/java/basis/generics-and-wildcards.md#什么是泛型擦除机制为什么要擦除)。本章不用实现桥方法，也不需要先学反射。
:::

## 包装类型与空值

### 基本类型对应的对象类型

Java 的基本类型用于直接保存数值等数据；泛型类型参数需要引用类型，所以集合用它们对应的包装类。

| 基本类型 | 包装类 | 基本类型 | 包装类 |
|---|---|---|---|
| byte | Byte | short | Short |
| int | Integer | long | Long |
| float | Float | double | Double |
| char | Character | boolean | Boolean |

这些类位于 java.lang，无须单独 import。包装对象本身不可变，例如 Integer 中表示的整数不会被改成另一个整数。`Integer count = 1; count++;` 会计算新值、重新装箱并给变量赋值，不是在原对象内修改数字。

Java 可以自动装箱和拆箱：

```java
Integer boxed = 7;       // 装箱，可理解为 Integer.valueOf(7)
int plain = boxed;      // 拆箱，可理解为 boxed.intValue()
List<Integer> ids = new ArrayList<>();
ids.add(plain);         // add 需要 Integer，自动装箱
int firstId = ids.get(0); // get 返回 Integer，自动拆箱
```

因此上一章 `Map<Integer, Task>` 的 `get(1)` 可以直接传 int。装箱提供语法上的方便，没有让 int 和 Integer 完全相同：int 没有 null，Integer 可以是 null。

### 拆箱的 NullPointerException

```java
Map<String, Integer> counts = new HashMap<>();
counts.put("待办", 3);
Integer found = counts.get("完成"); // 未找到，得到 null
// int completed = found;          // 取消注释：拆箱时抛出 NullPointerException
int completed = found == null ? 0 : found;
System.out.println(completed);    // 0
```

`int completed = counts.get("完成")` 虽然没出现方法调用，背后仍要对返回的 Integer 调用 intValue。返回 null 时就失败了。是否把“缺失”看作 0 是业务约定：统计数量时可能合理，查询用户年龄时可能应该保留“未知”。

`getOrDefault("完成", 0)` 可以处理没有该键的情况，但如果 Map **确实保存了这个键且值为 null**，它仍会返回 null。不能把这个方法当成所有空值的兜底。

### Integer 的比较

```java
Integer a = 42;
Integer b = 42;
Integer c = 1000;
Integer d = 1000;
System.out.println(a == b);        // true：小整数使用缓存
System.out.println(c.equals(d));   // true：按 Integer 数值比较
System.out.println(c == 1000);     // true：与 int 比较时拆箱
System.out.println(a.equals(42L)); // false：Long 不是 Integer
```

当两边都是 Integer 时，`==` 比较引用。Integer.valueOf 保证缓存 -128 到 127，也可能缓存其他数值；所以不能凭某台机器上 `c == d` 的结果写业务判断，更不能把 127 以外都返回 false 当成规则。

两个非 null 的 Integer 比较数值用 equals；如果值允许 null，可以用 `Objects.equals(a, b)`。与基本类型比较时可能发生拆箱，先排除 null。

`Integer.equals` 要求另一个对象也是 Integer，再比较数值，所以 Integer 的 42 与 Long 的 42 不相等。需要比较不同数字类型时，应按业务要求明确转换；金额、精度和溢出问题不能靠一次强制转换解决。

依据：[OpenJDK 21：Integer.valueOf](https://github.com/openjdk/jdk21u/blob/master/src/java.base/share/classes/java/lang/Integer.java)。定位 valueOf 的文档，看保证的缓存范围，不需要阅读整个类。

## 引用相同与内容相等

`==` 对基本类型比较值，对对象引用比较是否指向同一个对象。equals 是实例方法，是否按内容比较取决于类的实现。

保存为 `EqualityIntro.java`：

```java
import java.util.Objects;

public class EqualityIntro {
    public static void main(String[] args) {
        String a = new String("阅读教材");
        String b = new String("阅读教材");
        String alias = a;
        System.out.println(a == b);
        System.out.println(a.equals(b));
        System.out.println(a == alias);
        System.out.println(Objects.equals(null, b));
        System.out.println(Objects.equals(null, null));
    }
}
```

输出依次为 `false`、`true`、`true`、`false`、`true`，各占一行。这里特意用 new 创建两个 String，便于观察不同对象；普通代码通常直接使用字符串字面量。

String 重写了 equals，比较字符内容。Object 默认的 equals 则等价于引用比较。自定义类如果没有重写它，字段一模一样也不代表相等：

```java
Task a = new Task(1, "阅读教材");
Task b = new Task(1, "阅读教材");
System.out.println(a == b);      // false
System.out.println(a.equals(b)); // false：当前 Task 没有重写 equals
```

`Objects.equals(a, b)` 是处理 null 的辅助方法，需要 `import java.util.Objects`。两边都是 null 返回 true；只有一边是 null 返回 false；否则按第一个对象的 equals 判断。它不会自动逐个比较对象字段。

### equals 的约定

自定义相等规则时，要满足下面的约定：

| 约定 | 含义 |
|---|---|
| 自反 | x.equals(x) 为 true |
| 对称 | x.equals(y) 与 y.equals(x) 结果相同 |
| 传递 | x 与 y 相等、y 与 z 相等，则 x 与 z 相等 |
| 一致 | 参与比较的信息没有变化时，多次比较结果相同 |
| 与 null 比较 | 非 null 的 x，x.equals(null) 为 false |

这里的 x、y、z 都是非 null 对象。不能把“数值很接近”随意写成 equals：例如差值小于 2 就相等，1 与 2 相等、2 与 3 相等，却让 1 与 3 不相等，破坏了传递性。近似比较应另设方法，不能代替集合需要的相等规则。

还要选择**哪些信息决定相等**。任务标题相同，不代表是同一条任务；标题改变，也不意味着任务换了身份。一个“坐标值”类可能按 x、y 比较，一个任务标识可能按 ID 比较，具体取决于类所表达的含义。

参考：[MOOC：Similarity of objects](https://github.com/rage/java-programming/blob/master/data/part-8/3-similarity-of-objects.md)，先看 equals，再看 HashMap 中的对象；[Object 的官方约定](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html#equals(java.lang.Object))。

## hashCode 与哈希集合

HashMap 不会每次都遍历所有键来查找。它先使用键的 hashCode 等信息缩小查找范围，再判断候选键是否相等。HashSet 用类似方式判断元素是否已经存在。

hashCode 返回 int。它必须与 equals 配合：

- equals 判定相等的对象，hashCode 必须相同。
- hashCode 相同的对象，不一定相等，这叫哈希碰撞。
- 参与相等判断的信息没变时，同一个对象的 hashCode 应保持一致；不要求跨程序运行得到同一个值。

因此重写 equals 时，通常也要重写 hashCode。只重写 equals，HashMap 可能把两个本应相等的键分到不同位置，让查询失败或产生重复条目。

**hashCode 不是唯一 ID，也不是对象的内存地址。** 不要用 `a.hashCode() == b.hashCode()` 替代 equals。

```java
Map<String, String> values = new HashMap<>();
values.put("Aa", "第一项");
values.put("BB", "第二项");
System.out.println("Aa".hashCode() == "BB".hashCode()); // true
System.out.println("Aa".equals("BB"));                // false
System.out.println(values.size());                    // 2
System.out.println(values.get("BB"));                 // 第二项
```

两个字符串的 hashCode 相同，Map 仍能区分它们。这是哈希集合本来就需要处理的情况。此处不用记桶下标计算、扩容和红黑树，先把相等规则写对。

## 为任务设计一个稳定的键

### ID 的范围

上一章每个 TaskManager 都有自己的 nextId，第一条任务都是 1。如果把多个管理器中的任务放进一个 Map，仅用整数 1 会互相覆盖。

这里约定工作空间 10 管理阅读任务，工作空间 20 管理编程任务。在各工作空间内，TaskManager 继续分配局部任务 ID。用两个数共同表达身份：`(10, 1)` 与 `(20, 1)` 是不同任务，`(10, 1)` 与另一个 `(10, 1)` 表达同一个任务。

工作空间号由这个演示手动分配，不是 TaskManager 已经提供的功能。实际项目要明确 ID 的生成与生命周期；当前任务还只存在内存中，重启后的身份恢复以后再实现。键也不承担权限校验。

### TaskKey 的实现

源码在 `projects/java-foundations/src/main/java/com/dailystudy/types/TaskKey.java`：

<<< ../../projects/java-foundations/src/main/java/com/dailystudy/types/TaskKey.java{java}

按三个方法分别看：

1. equals 先判断是否同一个引用，是就返回 true。然后判断对方是否为 TaskKey；null 或其他类型会返回 false。最后比较两个 ID。
2. hashCode 使用 `Objects.hash` 组合两个 ID。相等键的字段一致，因此能得到相同 hashCode；不同字段仍可能碰撞。
3. toString 只是显示成 `10:1`，便于阅读。它不参与 Map 的键判断。

`other instanceof TaskKey otherKey` 是模式匹配写法：检查类型成功时，把转换后的对象绑定到变量 otherKey。等价的传统写法是先 `other instanceof TaskKey`，再 `TaskKey otherKey = (TaskKey) other`。本仓库使用 Java 21，可以直接使用前者。

注意签名是 `equals(Object other)`。如果误写成 `equals(TaskKey other)`，只是新增一个重载，集合通过 Object 的方法约定调用时不会得到预期规则。`@Override` 能帮助编译器发现这种错误。

TaskKey 声明为 final，两个 int 字段也为 final，并且没有修改方法。final 类避免子类额外增加相等条件、破坏对称性；不可变字段让键在存入 Map 后保持稳定。这里字段都是基本类型；如果 final 字段指向一个可变 List，仅靠 final 并不能保证内容不可变。

这个类不把 Task 的 title、completed 放进相等判断。它描述任务身份，任务内容则继续放在 Map 的值中。

### 运行与查询

源码在同一包的 `GenericsEqualityDemo.java`：

<<< ../../projects/java-foundations/src/main/java/com/dailystudy/types/GenericsEqualityDemo.java{java}

在仓库根目录执行：

```bash
mvn -B -ntp -f projects/java-foundations/pom.xml test
java -cp projects/java-foundations/target/classes com.dailystudy.types.GenericsEqualityDemo
```

输出：

```text
42 的引用相同：true
1000 的数值相同：true
Integer 与 Long 相等：false
两个 null 相等：true
两个管理器的首个 ID：1, 1
两个 Task 相等：false
两个键的引用相同：false
两个键的内容相等：true
相等键的 hashCode 相同：true
选中的不同任务数：2
用新键查询：阅读泛型教材
改名后仍可查询：复习泛型与相等规则
另一工作空间的任务：练习对象相等
Aa 与 BB 的 hashCode 相同：true
碰撞后的条目数：2
分别查询：第一项, 第二项
```

original 与 same 是两个对象，equals 为 true，所以 Set 只保留一个相等的键。`new TaskKey(10, 1)` 虽然不是之前 put 的那个对象，也能查到对应任务。

值指向的 Task 可以改名、完成；这些修改不影响 TaskKey 的 hashCode，所以仍然能查询。我们没有把 Task 本身改成按局部 ID 比较相等，也没有更改上一章的状态规则。

源码中的查询键都已存在。如果换成不存在的键，get 会返回 null，直接调用 getTitle 会失败；自己的查询代码仍需先处理未找到的情况。

### 修改键参与比较的字段

下面是故意错误的设计。保存为 `MutableKeyDemo.java`：

```java
import java.util.HashMap;
import java.util.Map;

public class MutableKeyDemo {
    public static void main(String[] args) {
        MutableKey key = new MutableKey(1);
        Map<MutableKey, String> values = new HashMap<>();
        values.put(key, "阅读教材");
        key.id = 2;
        System.out.println(values.size());
        System.out.println(values.get(key));
    }
}

final class MutableKey {
    int id;

    MutableKey(int id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof MutableKey key && id == key.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
```

在本课程验证的 OpenJDK 21 中，输出为 `1`、`null`，各占一行。Map 内仍有条目，但存入时记录的是旧哈希信息，查询时使用新的信息。Map 不会因字段变化自动为键重新建立位置。

修改键会让 Map 的行为无法可靠依赖；不能因为某个例子暂时还能查到，就认为修改是安全的。HashSet 中参与相等判断的字段变化，也有同样的问题。

需要变更身份时，先用**未修改的旧键**移除条目，再用新的不可变键存入。需要改任务标题时，只改 Task 的标题，不改身份键。关键是参与 equals、hashCode 的信息在集合中保持稳定，不是所有键对象都必须没有任何可变字段。

## 练习

### 1. 类型检查的位置

把 TypedListDemo 中 `titles.add(100)` 取消注释。再运行 RawListDemo。两个错误分别发生在编译时还是运行时？不要只比较报错文字。

::: details 答案
带 String 类型参数的列表在编译时拒绝 Integer；原始类型绕过部分检查，直到运行时把 Integer 转成 String 才失败。编译警告也需要处理，能够生成 class 并不保证类型使用正确。
:::

### 2. 返回正确的类型

在 GenericIntro 中增加 `static <T> T last(List<T> values)`，拒绝空列表。分别让它返回字符串和整数；不要把返回类型改成 Object。

::: details 参考写法
```java
static <T> T last(List<T> values) {
    if (values.isEmpty()) {
        throw new IllegalArgumentException("列表不能为空。");
    }
    return values.get(values.size() - 1);
}
```

`String text = last(List.of("甲", "乙"));` 得到乙；`Integer id = last(List.of(1, 2));` 得到 2。方法声明中的 T 把元素类型与返回类型关联起来。
:::

### 3. 两种空值

分别执行下面的两次 getOrDefault。哪一次会返回 0？如果赋给 int 会怎样？

```java
Map<String, Integer> counts = new HashMap<>();
counts.put("待办", null);
Integer a = counts.getOrDefault("完成", 0);
Integer b = counts.getOrDefault("待办", 0);
```

::: details 答案
完成键不存在，a 是 0；待办键存在且值是 null，b 是 null。a 拆箱安全，b 拆箱产生 NullPointerException。如果业务把这两种情况都视为零，先获取 Integer，再明确检查 null；或者约定 Map 根本不存 null，并在写入时落实这个约定。
:::

### 4. 新对象作为查询键

依次 put 两个 `new TaskKey(10, 1)`，值分别是 A、B，再 put 一个 `new TaskKey(20, 1)`，值是 C。预测 Map 的大小、第二次 put 的返回值、用新键查询的结果。

::: details 答案
大小为 2；第二次 put 返回被替换的 A。用新 `(10, 1)` 查到 B，用新 `(20, 1)` 查到 C。Map 以相等键识别同一条目，不要求调用方一直保存最初的引用。
:::

### 5. 哈希碰撞

使用 `new TaskKey(1, 32)` 和 `new TaskKey(2, 1)`。当前 Objects.hash 实现给它们相同 hashCode，equals 是否也应该返回 true？加入 Map 后应有几条记录？

::: details 答案
equals 返回 false，应有两条记录。相同 hashCode 不能反推相等；这个例子也由 TaskKeyTest 验证。字段不同并不要求 hashCode 必须不同，更不能为了消除某次碰撞而改变业务身份规则。
:::

### 6. 重载与重写

把 TaskKey 的 equals 方法暂时替换成下面的写法，保留 `@Override`。观察编译结果；为什么删除注解不是修复？

```java
@Override
public boolean equals(TaskKey other) {
    return other != null && workspaceId == other.workspaceId && taskId == other.taskId;
}
```

::: details 答案
编译器指出它没有重写父类方法。删除注解只让这个重载方法能存在，Object 的 equals 仍未被重写；HashMap、HashSet 通过 Object 方法签名调用时会沿用引用比较。应恢复 `equals(Object other)`，保留注解。
:::

### 7. 可变键与可变值

任务改名后为什么能继续查询，而 MutableKeyDemo 里的键改 ID 后查询失败？如果将任务标题也加入键的 equals 和 hashCode，会发生什么？

::: details 答案
前者只修改值指向的 Task，没有改变键；后者修改了键参与定位的信息。若键的比较直接依赖一个会改变的任务标题，改名也可能破坏查找。若只是复制了一个 String 标题作为不可变键字段，哈希位置不变，但它仍不能准确表达“任务改名后身份不变”的业务含义。任务键应基于稳定的身份，而不是标题。
:::

### 8. 通配符（补充练习）

为什么 NumbersDemo 的 sum 能同时接收整数列表和小数列表？能否在 sum 内执行 `values.add(10)`？addSample 能否接收 `List<Double>`？

::: details 答案
sum 使用 `List<? extends Number>`，能读取任何 Number 子类型列表的元素。它不知道具体元素类型，不能添加 10，因为实际列表可能是 Double 列表。addSample 不能接收 `List<Double>`：Double 不是 Integer 的父类型。需要同时读写确定类型时使用明确的类型参数，不要随意用强制转换消除错误。
:::

## 对应阅读

- [MOOC：Type parameters](https://github.com/rage/java-programming/blob/master/data/part-12/1-type-parameters.md)：自定义泛型容器、不同类型参数，适合先看例子再动手写。
- [MOOC：Similarity of objects](https://github.com/rage/java-programming/blob/master/data/part-8/3-similarity-of-objects.md)：equals、hashCode 与对象作为 Map 键，重点看车牌示例。
- [Javaer：泛型](https://github.com/itwanger/toBeBetterJavaer/blob/master/docs/src/basic-extra-meal/generic.md)、[equals 与 ==](https://github.com/itwanger/toBeBetterJavaer/blob/master/docs/src/string/equals.md)：中文补充解释。JavaGuide 的[泛型与通配符](https://github.com/Snailclimb/JavaGuide/blob/main/docs/java/basis/generics-and-wildcards.md) 适合做章节后的归纳。
- [Java 21：Object](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html)、[Integer](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Integer.html)、[Objects](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Objects.html)：核对相等约定、缓存、空值处理；官方站点不便访问时，可阅读 [OpenJDK Object 源码中的方法文档](https://github.com/openjdk/jdk21u/blob/master/src/java.base/share/classes/java/lang/Object.java) 和前文的 Integer 源码。

本文的例子和讲解为本仓库重新编写，参考资料用于知识顺序与规则核对。暂时不要求阅读 HashMap 的完整实现。

下一章学习异常与调用栈：出错以后哪一段代码停止、异常怎样向上传递，以及在哪里处理才能保留正确的业务状态。完整安排见[学习路线](/learning-path)。
