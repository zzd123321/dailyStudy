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

从 Mac 的仓库根目录执行：

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
- [Java 官方学习：语言基础](https://dev.java/learn/language-basics/)：按变量、运算符、控制流选读。
- [Java 21 Scanner 文档](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Scanner.html)：按需查 nextLine 与 hasNextLine，不必通读所有方法。

正文参考公开教程中“先运行例子、再逐步改变条件”的讲法，用本仓库的计算器串联知识。能独立修改规则并解释输出，比记住全部语法更重要。

下一课 [请求链路与服务端状态](/lessons/request-lifecycle)，把今天的输入、解析、判断和方法放回 HTTP 请求中，追踪一条笔记到底怎样被创建。
