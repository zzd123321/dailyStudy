---
description: 把固定数字改成用户输入，通过学习时长计算器学会类型转换、判断、循环、方法和错误处理。
---

# 03 · Java 输入、控制流与方法

第 01 课的程序写死了每周四天、每天 2.5 小时，所以无论谁运行，结果都是每周 10 小时。

现在我们让使用者自己输入，并让程序回答三个问题：一周多少小时？是否符合每周 8–12 小时的当前计划？连续三周会累计多少小时？

这些问题分别需要**输入、条件判断、循环**。最后把计算提取成方法，你就能看懂很多后端逻辑的基本结构。

## 先运行一个足够小的完整程序

这是完整源码，不需要补其他文件。它也已经放在仓库的 `lessons/day-003/examples/InputBasics.java` 中：

<<< @/../lessons/day-003/examples/InputBasics.java

从仓库根目录执行：

```bash
cd lessons/day-003/examples
mkdir -p out
javac -encoding UTF-8 -d out InputBasics.java
java -cp out InputBasics
```

按提示每次输入一行，依次输入 `小林`、`4`、`2.5`，每行按回车。预期结果包括：

```text
小林每周计划学习 10.0 小时。
符合当前学习节奏。
第 1 周累计：10.0
第 2 周累计：20.0
第 3 周累计：30.0
```

先使用这些合法数字。这个最小例子暂时不处理乱输内容；后半课再给它加上错误处理。接下来每段代码都取自这个程序或是明确标注的练习片段。

## 输入进来的是文字，计算需要数字

看这三行：

```java
String name = input.nextLine();
int days = Integer.parseInt(input.nextLine());
double hours = Double.parseDouble(input.nextLine());
```

`nextLine()` 读取到回车之前的一整行，结果是 String。即使键盘输入 `4`，刚读到的也是文本 `"4"`。

计算前，需要把文本转换成数字：

```text
键盘输入 4 → nextLine 得到 "4" → parseInt 得到整数 4
键盘输入 2.5 → nextLine 得到 "2.5" → parseDouble 得到小数 2.5
```

可以与前端经验对照：JavaScript 的 `Number('4')` 也是转换，但 Java 的 `Integer.parseInt("四")` 会抛出异常，而不是返回 NaN。两种语言的失败行为不同。

| 类型 | 本例变量 | 保存什么 |
|---|---|---|
| `String` | name | 文本，如“小林” |
| `int` | days | 整数，如 4 |
| `double` | hours | 浮点数，如 2.5 |
| `boolean` | 条件表达式的结果 | true 或 false |

Java 的 `int` 与 `double` 是不同类型。这里天数只允许整数，小时数可以有小数，因此分别选择它们。

### Scanner 是怎样接到键盘的

```java
Scanner input = new Scanner(System.in, StandardCharsets.UTF_8);
```

`System.in` 是程序的标准输入，交互运行时通常来自终端。Scanner 帮你从这个输入源读取数据；`input` 是我们给读取工具起的变量名。

文件开头的 `import` 告诉编译器 Scanner 和 StandardCharsets 来自哪里，不会启动另一个程序。

最外面的 `try (...) { ... }` 会在代码块结束时关闭 Scanner。这叫资源管理，先会使用；学文件、数据库连接时再讲它的完整原理。

::: details 为什么统一使用 nextLine，而不是混用 nextInt？
`nextInt()` 读取数字 token，通常留下行尾换行；紧接着 `nextLine()` 可能只读到剩下的空行。这很容易让初学者误以为程序跳过了输入。

本例统一“读取一行 → 转换”，输入处理更一致。不是说 nextInt 不能用，而是现在选择更容易推演的方式。

浮点数也不能精确表示所有十进制小数，例如 0.1。当前时长估算使用 double 足够；金额等需要明确精度与舍入规则时，会学习 BigDecimal。
:::

## 计算规则：为什么 10 / 4 得不到 2.5

先在 main 内试三个表达式：

```java
System.out.println(10 / 4);       // 2
System.out.println(10 / 4.0);     // 2.5
double average = 10 / 4;
System.out.println(average);      // 2.0
```

第三个结果最值得注意：右边先按两个整数计算，得到 2，再放进 double 成为 2.0。**接收变量是 double，不会让已经发生的整数除法重新计算。**

整数除法向零截断。要计算平均值，可以让一个操作数先成为 double：

```java
int totalHours = 10;
int days = 4;
double average = (double) totalHours / days;
```

`(double)` 是显式类型转换。它与 `Double.parseDouble("2.5")` 不同：前者转换已有数值，后者解析文本。

### 取余、累计和优先级

`%` 得到余数，可以判断每三周是否安排一次阶段检查。以下是循环内可用的片段：

```java
if (week % 3 == 0) {
    System.out.println("本周安排阶段检查。");
}
```

`accumulated += weeklyHours` 在当前 double 变量上表示“把一周时长加到累计值”；`week++` 则把 week 加一。尽量单独写更新语句，不把多次自增塞进一个表达式。

乘除先于加减，比较先于 `&&`、`||` 中的组合。复杂条件用括号说明分组，比要求读者背优先级表更清楚：

```java
boolean inRange = (days >= 1 && days <= 7);
boolean needsAdjustment = (weeklyHours < 8.0 || weeklyHours > 12.0);
```

int 也有范围限制：它不是无限精度整数，过大的运算可能溢出；double 也有浮点舍入。普通学习时长够用，但记录金额、巨大 ID 或精确统计时，需要按业务选择类型。

## 字符串处理：去空白、比较和检查长度

读取名字后，常见处理是：

```java
String rawName = "  小林  ";
String name = rawName.strip();
System.out.println(rawName); // 仍含首尾空白。
System.out.println(name);    // 小林。
System.out.println(name.isEmpty()); // false。
```

String 的内容不可变。strip 返回处理后的字符串，不会就地改掉 rawName。后续 `text = text.strip()` 是把变量改为指向处理结果，不是修改原字符串内容。

| 方法 | 适合解决的问题 |
|---|---|
| `strip()` | 去掉首尾空白 |
| `isEmpty()` | 长度是否为 0 |
| `isBlank()` | 是否为空或全是空白 |
| `equals(...)` | 内容相同吗 |
| `equalsIgnoreCase(...)` | 忽略大小写后相同吗 |
| `length()` | UTF-16 单元数量 |

`"   ".isEmpty()` 是 false，`"   ".isBlank()` 是 true。它们检查的规则不同；去空白后再检查非空，是另一种表达同一业务要求的方式。

第 02 课标题上限按 Unicode 码点计算，不能把 String.length 无条件当成用户眼中的字符数量。例如 emoji 可能占两个 UTF-16 单元。当前先记住“判断长度要明确计数规则”。

## if：程序怎样根据数字作决定

现在已经算出 `weeklyHours`，例如 10.0：

```java
if (weeklyHours >= 8.0 && weeklyHours <= 12.0) {
    System.out.println("符合当前学习节奏。");
} else {
    System.out.println("需要调整任务量或学习时间。");
}
```

用中文读它：**如果每周小时数至少 8，且不超过 12，就输出符合；否则输出需要调整。**

`>=` 和 `<=` 含边界，所以 8.0、12.0 都符合。`&&` 表示两个条件同时成立；`||` 表示至少一个成立；`!` 表示取反。

把输入改成两天、每天两小时，得到 4.0。第一个条件已经不成立，程序进入 else。

### 多于两种结果，用 else if

如果你希望分别说明投入偏低、符合、偏高，可把原判断替换为这个片段：

```java
if (weeklyHours < 8.0) {
    System.out.println("投入偏低，可以缩小每周目标。");
} else if (weeklyHours <= 12.0) {
    System.out.println("符合当前学习节奏。");
} else {
    System.out.println("投入偏高，注意留出休息时间。");
}
```

为什么第二个条件不再写 `>= 8.0`？因为只有前面的 `< 8.0` 不成立，才有机会来到这里。分支按顺序检查，只执行第一条满足条件的分支。

**判断顺序也是逻辑的一部分。** 不能把 `<= 12.0` 放在最前面，否则 4.0 也会被误判为符合。

::: details 赋值、数字相等和字符串相等
`=` 是赋值，`==` 是比较。Java 的 if 条件必须是 boolean，不像 JavaScript 那样直接把任意数值当真假。

数字可以用 `days == 4`；比较字符串内容用 `text.equals("q")`。`==` 对引用比较的是是否指向同一对象，不应拿来判断两个输入字符串的内容是否相同。

想让 q 和 Q 都能退出，可用 `text.equalsIgnoreCase("q")`。调用方法前还要知道 text 是否可能为 null，后面的完整程序会处理这一点。
:::

### 短路判断：左边不满足，右边就不再执行

假设 text 可能为 null：

```java
String text = null;
if (text != null && !text.isBlank()) {
    System.out.println(text);
}
```

`&&` 左边已经是 false，就不会继续调用右边的方法。这叫短路，能避免在 null 上调用 isBlank。

也可以把无效条件写在一起：

```java
if (text == null || text.isBlank()) {
    System.out.println("没有可用文本。");
}
```

`||` 左边已经为 true，右边也不需要执行。顺序不能随便换；先调用 text.isBlank，再判断 null，保护就失效了。

布尔运算中的 `&`、`|` 不使用同样的短路规则，不要把它们当作 `&&`、`||` 的简写。

### 离散选项用 switch，数值区间继续用 if

用户输入的是 y、n 或其他固定选项时，可以用 switch。下面是 Java 21 可用的语句片段，放在 main 内：

```java
String choice = "y";
switch (choice) {
    case "y", "Y" -> System.out.println("继续计算。");
    case "n", "N" -> System.out.println("结束计算。");
    default -> System.out.println("请输入 y 或 n。");
}
```

箭头写法不会自动接着执行下一个分支。每周时长低于 8、介于 8 与 12、超过 12，则更适合用 if 表达区间。

先根据问题选表达方式，而不是为了使用新语法，把原本清楚的逻辑改复杂。本段假设 choice 非 null，完整输入处理仍要先检查取消或输入结束。

## for：重复做一件事，但每次周数不同

不必手写三条打印语句：

```java
for (int week = 1; week <= 3; week++) {
    System.out.println("第 " + week + " 周累计：" + (week * weeklyHours));
}
```

括号中的三部分，按这个顺序起作用：

1. `int week = 1`：开始之前执行一次，从第 1 周算起。
2. `week <= 3`：每次执行循环体之前判断，符合才继续。
3. `week++`：本轮打印后，把周数加 1，再回到判断。

每周 10 小时时，完整推演是：

| 检查时的 week | 条件是否成立 | 打印累计时长 | 随后发生什么 |
|---:|---|---:|---|
| 1 | 成立 | 10 | week 变成 2 |
| 2 | 成立 | 20 | week 变成 3 |
| 3 | 成立 | 30 | week 变成 4 |
| 4 | 不成立 | 不打印 | 循环结束 |

注意不是“跑完第三次就自动停”，而是下一次条件检查不成立才停。把 `<= 3` 改为 `< 3`，就只打印前两周。

本例直接用 `week * weeklyHours` 算累计。也可以用一个变量每次加一周时长，两者在每周时长固定时得到相同结果。

## 数组：把多次学习时长放到一起处理

之前每周时长都一样，所以直接相乘。现在四次学习分别是 2.0、2.5、3.0、2.5 小时，需要保存一组数据：

```java
double[] hours = {2.0, 2.5, 3.0, 2.5};
System.out.println(hours[0]);     // 2.0，第一项。
System.out.println(hours.length); // 4，元素数量。
```

`double[]` 表示 double 数组。下标从 0 到 length-1；本例最后一项是 hours[3]，访问 hours[4] 会越界。

Java 数组创建后长度固定，不能直接像 JavaScript 数组一样 push。后面集合课会引入 ArrayList，处理可变数量的业务对象。

### 用下标循环求总和

```java
double total = 0.0;
for (int index = 0; index < hours.length; index++) {
    total += hours[index];
}
System.out.println(total); // 10.0。
```

这个循环从 0 开始，因此结束条件是 `< hours.length`，不能写 `<=`。每轮读取一项，加进 total。

第一次 total 是 2.0，第二次 4.5，第三次 7.5，最后 10.0。把每轮变量写出来，是检查循环逻辑最直接的方法。

### 只需要元素时，用增强 for

同样的求和也可以写成：

```java
double total = 0.0;
for (double value : hours) {
    total += value;
}
```

用中文读成“对 hours 中的每个值”。它适合只关心元素、不需要下标的遍历；需要显示“第几次”、按位置更新或访问邻项时，下标循环更合适。

把数组交给方法，方法就能处理任意数量的时长。一个常见错误是直接打印数组变量，得到类似 `[D@...` 的信息；要展示内容，遍历元素或使用 `Arrays.toString(hours)`。后者需导入 `java.util.Arrays`。

## 方法：给一段计算起名字

main 中调用：

```java
double weeklyHours = calculateWeeklyHours(days, hours);
```

类中定义：

```java
static double calculateWeeklyHours(int days, double hours) {
    return days * hours;
}
```

可以读成：“这个叫 calculateWeeklyHours 的方法，接收一个整数和一个浮点数，计算乘积，并返回一个浮点数。”

```text
调用时传入 4 和 2.5
       ↓
方法参数 days=4，hours=2.5
       ↓
return 4 * 2.5
       ↓
调用处得到 10.0，存入 weeklyHours
```

方法的参数是它收到的数据，`return` 是把结果交给调用处，并结束本次方法执行。这里的 static 让 main 能直接调用这个方法；实例方法等学完对象再比较。

### 返回和打印，用途不同

`System.out.println(10.0)` 只是向终端显示，不能把显示出来的文字当成计算返回值。`return 10.0` 则把结果交给调用者，后者可以继续用于判断、循环或保存。

这与前端函数一样：计算函数返回数据，页面决定怎样展示。把两件事分开，后面同一个计算逻辑就能用于终端、HTTP 接口或测试。

::: details 方法放在哪里？变量在哪里能访问？
Java 的普通方法定义在类的内部、其他方法的外部，不要把方法声明塞进 main。

```java
public class Example {
    public static void main(String[] args) {
        double result = twice(2.5);
        System.out.println(result);
    }

    static double twice(double value) {
        return value * 2;
    }
}
```

main 中的 result 是局部变量；twice 中的 value 是该方法的参数。twice 不能直接访问 main 的 result，需要通过参数传入数据。for 声明的 week 也不能在循环结束后随意使用。

参数名与调用者的变量名不必相同，传递的是值。现在先用基本类型理解，之后对象课再展开引用值的传递。
:::

### 一个方法调用另一个方法：沿着结果推演

方法不只由 main 调用。下面两个定义应放在类内部、main 外部：

```java
static double sum(double[] hours) {
    double total = 0.0;
    for (double value : hours) {
        total += value;
    }
    return total;
}

static double average(double[] hours) {
    if (hours.length == 0) {
        throw new IllegalArgumentException("至少需要一次学习时长");
    }
    return sum(hours) / hours.length;
}
```

调用 average 时，先检查长度，再调用 sum，sum 的结果返回后再做除法。这里总和已经是 double，所以不是前面的整数除法。

空数组求和为 0 很自然，但平均值没有分母，规则应明确拒绝。`throw` 主动抛出异常，停止这次正常计算；后面会用 catch 接住预期错误。这个开头先排除无效情况的写法叫提前校验或守卫条件，能减少后面嵌套。

以上方法约定数组非 null、元素已经校验；它们不自动把任何外部输入都变成可信数据。

### 基本类型参数：方法内改了，不会改掉调用者的变量

以下是完整小程序：

```java
public class ValueDemo {
    public static void main(String[] args) {
        int days = 4;
        increase(days);
        System.out.println(days); // 4。
    }

    static void increase(int value) {
        value++;
        System.out.println(value); // 5。
    }
}
```

参数 value 得到的是数值 4 的副本。改它不等于改 main 的 days；想让调用处拿到新结果，通常让方法返回，再赋值。

Java 都是按值传递。数组和对象会传递引用值的副本，方法通过该引用修改同一个数组元素时，调用者可看到修改；把参数重新指向另一数组则不等于重新赋值调用者变量。对象课会继续用实例解释。

方法可以同名但参数列表不同，这叫重载；仅仅把返回类型从 int 改成 double，不足以构成两个重载方法。现在先把方法名、参数与返回值写清楚，不必为每次计算设计一堆重载。

## 错误输入：读得懂，不等于允许使用

现在试着在天数里输入 `四`，最小程序会报 `NumberFormatException`；输入 `9` 则能转换成数字，却不符合“一周最多七天”的规则。

这是两类问题：

```text
"四" → 无法转换成整数 → 解析失败
"9"  → 能转换成整数 9 → 规则校验失败
"4"  → 能转换成整数 4 → 规则校验通过
```

第 02 课的 JSON 解析与字段校验也是这个区别：先读懂输入，再判断是否可接受。

### 把一次读取变成“直到合法或退出”

下面是一个**完整独立练习程序**。可以在同一个 examples 目录新建 `ReadDays.java`，编译后运行：

```java
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class ReadDays {
    public static void main(String[] args) {
        try (Scanner input = new Scanner(System.in, StandardCharsets.UTF_8)) {
            while (true) {
                System.out.print("天数（1–7），q 退出：");
                if (!input.hasNextLine()) break;
                String text = input.nextLine().strip();
                if (text.equalsIgnoreCase("q")) break;

                try {
                    int days = Integer.parseInt(text);
                    if (days >= 1 && days <= 7) {
                        System.out.println("已接受：" + days);
                        break;
                    }
                    System.out.println("数字要在 1–7 之间。");
                } catch (NumberFormatException error) {
                    System.out.println("请使用整数，例如 4。");
                }
            }
        }
    }
}
```

```bash
javac -encoding UTF-8 -d out ReadDays.java
java -cp out ReadDays
```

这里 `while (true)` 表示反复读取。输入不合格时，提示后进入下一轮；输入合法或 q 时，`break` 结束最近的循环。

`try/catch` 在 parseInt 失败时接住异常，给出提示，而不是让程序直接中断。只捕获预期的数字格式错误，别用“接住所有错误并继续”掩盖程序缺陷。

`hasNextLine()` 处理输入流结束：如果已经没有下一行，就结束读取。`strip()` 去掉首尾空白，因此 ` 4 ` 也可被接受。

请按 `四 → 9 → 4` 的顺序输入。你应分别看到解析错误、范围错误和接受结果。再运行一次输入 q，观察正常结束。

::: details break、return 和 continue 分别结束什么？
- `break`：离开最近的循环，本方法后面的代码仍可继续。
- `return`：结束本次方法执行，有返回类型时交出结果。
- `continue`：跳过当前轮剩余代码，进入下一轮循环。

选择 while 还是 for，取决于问题：已知要预览三周，for 很自然；不知道用户会输错几次，while 更自然。两者不是不同难度等级。
:::

## 最后阅读完整版本，看看小知识怎样组合

仓库里的完整版增加了称呼、天数、小时、周数校验，并支持重复计算。先从仓库根目录运行：

```bash
cd projects/java-foundations
mvn -B -ntp test
java -cp target/classes com.dailystudy.day003.InteractiveStudyPlanner
```

如果还停在 examples 目录，先回到仓库根目录再执行，不要在错误目录继续追加路径。

这版的规则为：称呼非空；天数 1–7；小时 0.5–6.0 且有限；周数 1–52。每个提示都接受 q 退出，计算后 y 继续、n 结束；输入流结束也会正常退出。

例如输入 `小林、4、2.5、4、n`（逐行输入），会打印每周 10.00 小时、四周 40.00 小时，以及前三周预览。若只计划两周，就只显示两周。

别一次硬读完整类。按下表找相应方法，每次回答“输入是什么、输出是什么、哪里结束”：

| 方法 | 看懂它负责什么 |
|---|---|
| `readLine` | 统一读取、去空白，识别退出 |
| `readName` | 名称不合格就再问 |
| `readInt` | 通用的整数解析与范围校验 |
| `readHours` | 小数解析、有限数与范围校验 |
| `calculateWeeklyHours` | 只负责一周时长计算 |
| `printReport` | 负责显示与累计预览 |
| `run` | 把整个交互按顺序组织起来 |

::: details 展开完整源码
<<< @/../projects/java-foundations/src/main/java/com/dailystudy/day003/InteractiveStudyPlanner.java
:::

::: details 完整版里的 Integer、Double 和 null
`int` 只能存整数，不能为 null；`Integer` 是包装类型，可以保存整数或 null。这里 readInt 用 null 约定“用户取消或输入结束”，不是数字 0。

run 先检查 `days == null`，再交给计算方法使用。若直接把 null 转成 int，会出现 NullPointerException。Double 与 double 也有类似区别。

`Double.parseDouble` 还接受 NaN、Infinity，所以解析成功之后仍检查 `Double.isFinite`。这些内容不代表允许的学习小时。

`printf` 中 `%.2f` 表示显示两位小数，`%n` 表示换行；Locale.ROOT 使格式不受电脑区域设置影响。这是显示规则，不改变底层数字。
:::

## 综合实践：统计一组真实学习时长

把前面的知识组合起来，写一个可以接收任意多条小时数的程序。它不是新的学习记录功能，只是本课的数组与方法练习。

输入要求：每项为 0–6 的有限数字，0 表示没有学习；没有命令行参数时使用示例数组。输出总时长、平均时长，以及达到 2.5 小时的次数。

把以下完整源码保存为练习目录中的 `StudyReport.java`：

```java
import java.util.Locale;

public class StudyReport {
    public static void main(String[] args) {
        String[] texts = args.length == 0
                ? new String[]{"2.0", "2.5", "3.0", "2.5"}
                : args;
        try {
            double[] hours = parseHours(texts);
            System.out.printf(Locale.ROOT, "总时长：%.2f%n", sum(hours));
            System.out.printf(Locale.ROOT, "平均时长：%.2f%n", average(hours));
            System.out.println("达到 2.5 小时的次数：" + countAtLeast(hours, 2.5));
        } catch (IllegalArgumentException error) {
            System.out.println("输入不合格：" + error.getMessage());
        }
    }

    static double[] parseHours(String[] texts) {
        double[] values = new double[texts.length];
        for (int i = 0; i < texts.length; i++) {
            double value = Double.parseDouble(texts[i]);
            if (!Double.isFinite(value) || value < 0.0 || value > 6.0) {
                throw new IllegalArgumentException("小时须为 0–6 的有限数字");
            }
            values[i] = value;
        }
        return values;
    }

    static double sum(double[] hours) {
        double total = 0.0;
        for (double value : hours) total += value;
        return total;
    }

    static double average(double[] hours) {
        if (hours.length == 0) {
            throw new IllegalArgumentException("至少需要一次学习时长");
        }
        return sum(hours) / hours.length;
    }

    static int countAtLeast(double[] hours, double target) {
        int count = 0;
        for (double value : hours) {
            if (value >= target) count++;
        }
        return count;
    }
}
```

从保存该文件的目录执行：

```bash
mkdir -p out
javac -encoding UTF-8 -d out StudyReport.java
java -cp out StudyReport
java -cp out StudyReport 1 2 3
java -cp out StudyReport 0
java -cp out StudyReport 四
java -cp out StudyReport -1
java -cp out StudyReport NaN
```

默认结果是 10.00、2.50、3 次。`1 2 3` 得到 6.00、2.00、1 次。0 是合法记录，其他三个非法输入则给出提示，不输出统计结果。

这里新出现的 `条件 ? 值一 : 值二` 是三元表达式：条件成立选择值一，否则选择值二。`new double[n]` 创建 n 个元素的数组，初始值都是 0.0；之后循环逐项填入。

### 为什么把读取、校验、统计拆开

parseHours 处理不可信文本；sum、average、countAtLeast 处理已经合格的数字；main 组织调用和显示。以后从 HTTP 或文件接收输入，可以复用统计方法，不必把它们全部重写。

这是方法拆分的真正用途：**职责稳定时，让输入来源和显示方式可以改变**。不是看到十行代码就机械拆成十个方法。

NumberFormatException 是 IllegalArgumentException 的子类，所以这里的 catch 也会接住数字格式错误。它们的继承关系下一课再展开，当前只理解这个明确的输入边界。

### 进一步改三件事

1. 新增 maximum，返回最长一次时长；空数组应该怎样处理？
2. 新增 countBelow，统计低于目标的次数；0 是否计入？
3. 把命令行数组改为已有 Scanner 交互输入，统计函数是否需要改变？

::: details 思路与边界
maximum 可先排除空数组，再从第一项开始作为当前最大值，逐项比较更新。不要无条件用 0 当所有业务的最大值初始值，那会在允许负数的其他问题中出错。

countBelow 逐项判断 `< target`；按本题定义，0 是一次合法记录，也应计入。若改成“有学习的次数”，条件应改为 `value > 0`，业务规则不同。

改输入来源时，只改收集与转换数字的部分，统计方法可以继续复用。先保持规则一致，再验证正常与非法输入。
:::

## 从报错和断点中理解执行顺序

读异常时，先找异常类型与消息，再找第一条属于自己源码的调用位置。例如 parseDouble 出错后，调用栈会带你回到 parseHours，再回到 main。不要被上面的所有库内部行淹没。

用编辑器的 Java 调试功能，在 parseHours 的循环内下断点，以调试方式运行，观察 i、texts[i]、value；再在 sum 中观察 total。单步执行可以亲眼看到变量怎样改变。

- Step over：执行当前行，调用的方法通常整体完成。
- Step into：进入当前行调用的方法内部。
- Step out：完成当前方法，回到调用者。

如果停在读取键盘的行，程序仍可能需要你输入，不一定是“调试卡住”。循环越界、分支顺序和返回值错误，都适合用少量断点验证；这项能力会延续到 Spring Boot 与 AI 服务。

## 练习：自己改，而不是只运行原程序

### A. 预测分支边界

每周时长分别为 7.9、8.0、12.0、12.1，三分支版本会输出什么？

::: details 展开参考解释
依次为偏低、符合、符合、偏高。特别确认 8 和 12 都包含在中间区间中。
:::

### B. 修改循环

把最小程序改成预览五周。若每周 10 小时，最后一行是什么？再将条件改成 `< 5`，比较结果。

::: details 展开参考解释
用 `week <= 5`，最后是第 5 周累计 50.0；`week < 5` 则只到第 4 周 40.0。循环从 1 开始，结束条件决定哪些值能进入循环体。
:::

### C. 新增一个返回结果的方法

在 InputBasics 类中新增 `calculateTotalHours(double weeklyHours, int weeks)`，main 调用它，打印四周总时长。方法只计算，不打印。

::: details 展开参考解释
把这个方法放在 main 外、类的大括号内：

```java
static double calculateTotalHours(double weeklyHours, int weeks) {
    return weeklyHours * weeks;
}
```

在 main 已有 weeklyHours 之后加入：

```java
double total = calculateTotalHours(weeklyHours, 4);
System.out.println("四周共 " + total + " 小时。");
```

本例每周 10 小时时，总数为 40.0。打印和计算分别在调用者和方法里。
:::

### D. 检查退出与重试

ReadDays 依次输入空行、2.5、8、4；再分别用 q 和输入流结束试一次。解释每一步是解析失败、范围失败还是正常结束。

::: details 展开参考解释
空行和 2.5 不能解析为整数；8 能解析但超范围；4 被接受并结束循环。q 在解析之前识别，输入流结束在读取之前识别，它们都不属于数字格式错误。
:::

### E. 找出混合职责

有人把 `System.out.println(days * hours)` 当成 calculateWeeklyHours 的实现，却仍声明返回 double。为什么不行？

::: details 展开参考解释
打印并没有返回 double，方法缺少 return，无法按该声明编译。计算方法应 `return days * hours`，调用者再决定怎样展示。
:::

## 资料怎么配合使用

- [二哥的 Java 进阶之路：基本数据类型](https://github.com/itwanger/toBeBetterJavaer/blob/master/docs/src/basic-grammar/basic-data-type.md)：补充 int、double、boolean。
- [二哥的 Java 进阶之路：流程控制](https://github.com/itwanger/toBeBetterJavaer/blob/master/docs/src/basic-grammar/flow-control.md)：对照分支与循环的例子。
- [Javaer：运算符](https://github.com/itwanger/toBeBetterJavaer/blob/master/docs/src/basic-grammar/operator.md)：重点读整数除法、取余和短路判断。
- [Javaer：数组](https://github.com/itwanger/toBeBetterJavaer/blob/master/docs/src/array/array.md)：重点读创建、下标、遍历；ArrayList 等后续再学。
- [Javaer：方法](https://github.com/itwanger/toBeBetterJavaer/blob/master/docs/src/oo/method.md)：当前先读方法声明、调用与返回，实例方法和继承部分下课衔接。
- [Java 官方学习：语言基础](https://dev.java/learn/language-basics/)：按变量、运算符、控制流选读。
- [Java 21 Scanner 文档](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Scanner.html)：按需查 nextLine 与 hasNextLine，不必通读所有方法。

正文参考公开教程中“先运行例子、再逐步改变条件”的讲法，用本仓库的计算器串联知识。能独立修改规则并解释输出，比记住全部语法更重要。

下一课 [请求链路与服务端状态](/lessons/request-lifecycle)，把今天的输入、解析、判断和方法放回 HTTP 请求中，追踪一条笔记到底怎样被创建。
