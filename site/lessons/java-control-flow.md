---
description: 从字符串输入到校验、循环和方法，逐步写出可以处理错误输入的 Java 交互式程序。
---

# 03 · Java 输入、控制流与方法

第 01 课的计算器只处理固定值，第 02 课展示了接口对输入进行校验。本课把它们串起来，构建一个交互式计划器：**读取输入 → 解析类型 → 校验规则 → 计算 → 输出 → 决定是否继续**。

先掌握最小例子里的类型、分支、循环和方法，再理解完整版的错误恢复。包装类型、异常和资源管理只解释本例用到的部分，后面会系统展开。

## 同步并运行最小例子

在 Mac 的仓库根目录，先保存自己的未提交练习，工作区干净时执行：

```bash
git pull --ff-only origin main
cd lessons/day-003
javac -encoding UTF-8 -d out examples/InputBasics.java
java -cp out InputBasics
```

按提示输入，**每输入一项按 Enter**：

```text
你的称呼：前端开发者
每周学习天数：4
每天学习小时：2.5
```

应看到每周 10.0 小时，以及前 3 周累计 10.0、20.0、30.0。

打开 examples/InputBasics.java。这个例子约三十行，用于认识核心语法，先输入合法数字；它尚未处理 abc、空白名称、负数等错误。后面的完整学习计划器会补齐校验，别把最小例子当作已具备全部输入保护的程序。

下面直接引用仓库的源码，阅读时可以对照每一步；后面的讲解会逐项拆开。

<<< @/../lessons/day-003/examples/InputBasics.java


## 类型与 TypeScript 的区别

| 用途 | Java 示例 | TypeScript 中的近似表达 |
|---|---|---|
| 文本 | `String name = "A";` | `const name: string = 'A';` |
| 整数 | `int days = 4;` | `const days: number = 4;` |
| 浮点数 | `double hours = 2.5;` | `const hours: number = 2.5;` |
| 布尔值 | `boolean valid = true;` | `const valid: boolean = true;` |

Java 将整数与浮点类型区分开。`int days = 2.5;` 无法直接编译，不能因为界面显示数字就忽略后端类型。

```java
int days = 4;
double hours = 2.5;
double weeklyHours = days * hours;
```

days 在参与浮点运算时转换为相应数值，结果使用 double。

回忆整数除法：

```java
System.out.println(5 / 2);    // 2
System.out.println(5.0 / 2);  // 2.5
```

此外，double 有浮点精度限制。本例报表保留两位小数便于阅读，计算中保留实际 double 值；金钱相关精确计算后续单独学习。


## 用 Scanner 读取一整行

```java
Scanner input = new Scanner(System.in, StandardCharsets.UTF_8);
String text = input.nextLine();
```

- System.in：程序的标准输入，通常连接终端。
- Scanner：读取输入的工具类，使用前需要 import。
- nextLine()：读入一整行，得到 String。
- 用户按 Enter 后，程序才能继续处理该行。

注意：**用户输入数字，看起来像数字，读入后仍可能是字符串。**

```java
String text = input.nextLine();
int days = Integer.parseInt(text);
```

将这个过程拆开，便于检查：输入是什么？能否解析？解析后范围是否合法？

### 为什么本课统一 nextLine

Scanner 的 nextInt() 读取数字 token，而 nextLine() 读取行。如果混用且不处理遗留的行尾，可能读到意外的空字符串。本课统一读整行，再显式转换，先减少这类干扰。

完整例子还会用 strip() 去掉首尾空白：输入 ` 4 ` 可以按 4 处理；名字只包含空白时则应拒绝。

源文件中的 `try (Scanner ...)` 会在结束时关闭该资源。它是资源管理语法；本课记住本 CLI 只创建一个 Scanner，退出后关闭即可。


## 输入能解析，不等于符合规则

下面三个输入的问题不同：

| 输入 | 能否作为整数解析 | 是否满足“天数为 1–7” |
|---|---|---|
| `4` | 可以 | 是 |
| `8` | 可以 | 否 |
| `abc` | 不可以 | 尚未进入范围判断 |

### 用 if 校验范围

```java
if (days >= 1 && days <= 7) {
    System.out.println("天数合法");
} else {
    System.out.println("请输入 1–7 之间的整数");
}
```

- `&&`：两个条件都要成立。
- `||`：任一条件成立即可。
- `!`：逻辑取反。
- Java 的 if 条件必须是 boolean。不能写 `if (days)` 来判断非零。

范围错误也可以反过来表达：

```java
if (days < 1 || days > 7) {
    System.out.println("天数超出范围");
}
```

### 用 if/else if/else 分类

```java
if (weeklyHours < 8.0) {
    System.out.println("低于建议投入");
} else if (weeklyHours <= 12.0) {
    System.out.println("符合当前节奏");
} else {
    System.out.println("高于建议投入");
}
```

判断从上到下执行，只进入第一个匹配分支。第二个分支执行时，第一个条件已经不成立，因此 weeklyHours 已经不小于 8。

先预测：7.99、8、12、12.01 分别进入哪个分支？边界值值得专门验证。

### 字符串比较

```java
if (choice.equalsIgnoreCase("y")) {
    System.out.println("继续");
}
```

字符串内容比较使用 equals；忽略大小写时使用 equalsIgnoreCase。`==` 对引用类型比较对象引用，不适合直接判断读入文字内容是否相同。

本例必须先确定 choice 不是 null，再调用其方法。


## for：重复已知次数的操作

```java
double accumulated = 0.0;
for (int week = 1; week <= 3; week++) {
    accumulated += weeklyHours;
    System.out.println("第 " + week + " 周累计：" + accumulated);
}
```

依次发生：

1. 初始化 week=1，只执行一次。
2. 判断 week<=3，成立才进入循环体。
3. 累加并输出。
4. 执行 week++，再回到条件判断。

输入每周 10 小时时，累计值是 10、20、30。`week <= 3` 包含第 3 次；改成 `< 3` 会少一次。

week 在 for 内声明，不能随意在循环外继续使用。变量的作用域由声明位置与代码块决定。

完整程序还检查实际计划周数，所以计划只有两周时不会预览第 3 周。


## while：等待有效输入或退出

输入可能连续出错，重复次数事先未知。适合用 while：

```java
while (true) {
    String text = input.nextLine();
    if (text.equalsIgnoreCase("q")) {
        break;
    }
    System.out.println("本次读到：" + text);
}
```

`while (true)` 本身不决定什么时候结束，必须设计出口。真实读取还要处理输入流结束；本课完整程序用 hasNextLine() 检查。

- break：离开当前循环。
- continue：跳过本轮剩余语句，进入下一轮。
- return：结束当前方法，可返回结果。

本课的完整程序主要使用 break 和 return；continue 留作练习理解。每次重试都必须读取新的输入，否则可能一直判断同一份错误数据。


## 方法：把计算从读取和打印中拆出来

```java
static double calculateWeeklyHours(int days, double hours) {
    return days * hours;
}
```

| 部分 | 含义 |
|---|---|
| static | 本课无需创建对象即可调用这个类的方法 |
| double | 返回值类型 |
| calculateWeeklyHours | 方法名 |
| int days, double hours | 参数及其类型 |
| return | 将结果交还调用方 |

调用：

```java
double weeklyHours = calculateWeeklyHours(4, 2.5);
```

方法定义写在类内部，与 main 并列；不要把 calculateWeeklyHours 的定义直接写进 main 方法体。调用语句则可以放在 main 中。

方法内部的参数值来自这次调用。`return` 与 println 不同：前者把结果交给调用方，后者产生输出。得到返回值后，你可以继续计算、判断或打印。

本例分工：

- 读取方法：获得有效输入，或识别取消。
- 计算方法：处理已经通过校验的数值。
- 分类方法：返回学习节奏说明。
- 报表方法：组织输出。

先写清楚这些小职责，再学习面向对象与框架，会更容易理解层次。


## 处理错误解析：一次最小 try/catch

```java
try {
    int value = Integer.parseInt(text);
    // 接着检查数值范围。
} catch (NumberFormatException e) {
    System.out.println("请输入整数");
}
```

parseInt 无法把 abc 转成整数时会抛出异常。catch 处理这类预期输入错误，再让读取循环重试。转换成功的 8 仍需范围校验，不能仅靠 try/catch 解决。

本课只认识这一类恢复操作，异常体系在后面的课程展开。不要用笼统的 catch(Exception) 把所有错误悄悄忽略。

Double.parseDouble 还能解析 NaN、Infinity 等特殊值；只写范围否定判断可能漏掉 NaN。完整程序用 Double.isFinite(hours) 先确认它是有限数，再校验 0.5–6.0 的范围。


## 类型转换：自动提升与强制转换

Java 会在允许的运算中提升数值类型，但不会把所有转换都当成安全操作：

```java
int days = 4;
double asDouble = days;       // 4.0，允许的整数到 double 转换
double hours = 2.5;
int truncated = (int) hours;  // 2，截去小数，不是四舍五入
```

强制转换 `(int)` 表示你明确要求改变数值表示，可能丢失信息。它不适合用来掩盖“学习小时需要小数”的业务需求，也不会把字符串解析成数字；字符串仍要使用解析方法。

```java
double ratio = 5 / 2;          // 2.0，整数除法已经先完成
double exactRatio = 5.0 / 2;   // 2.5
double another = (double) 5 / 2; // 2.5
```

左边变量是 double，不会反过来改变右边已经发生的运算。先看操作数，再看运算结果，最后看赋值。

Java 的整数有有限范围，`int` 不是任意大小的数字。`Integer.parseInt("999999999999")` 会抛出 `NumberFormatException`，因为结果无法用 int 表示。以后涉及 ID、金额和大数量时，应按业务选择类型。

## 从一个输入，推演完整处理流程

对“每周天数”使用四个输入，逐步检查不同阶段：

| 输入 | 去空白后 | 整数解析 | 范围检查 | 结果 |
|---|---|---|---|---|
| ` 4 ` | `4` | 成功得到 4 | 1–7，合法 | 返回 4 |
| `8` | `8` | 成功得到 8 | 不合法 | 提示并重试 |
| `abc` | `abc` | 抛异常 | 不执行 | 提示并重试 |
| `q` | `q` | 不尝试 | 不执行 | 取消读取 |

q 应在数字解析前处理，否则会被当成普通非法数字。完整程序用一个读取方法统一处理去空白、取消和输入结束，再由具体读取方法进行解析与校验。

### 不要用一个默认值同时表示失败与合法结果

```java
// 容易出错的约定：解析失败时返回 0。
```

如果某项业务允许 0，就无法判断这是用户真的输入 0，还是转换失败。当前计划器选择 `Integer`/`Double` 加 null 表示取消，合法值则明确在范围内。这个约定需要调用方检查：

```java
Integer days = readInt(input, out, "每周天数：", 1, 7);
if (days == null) {
    // 结束当前计算流程，不使用这份输入。
    return;
}
```

这是完整类内部的调用片段，不是单独可运行的程序。`Integer` 是包装类型，能容纳 null；`int` 不能。对 null 自动拆箱会抛出异常，所以应先处理取消，再参与计算。以后还会学习更明确的结果类型。

## 布尔运算与短路：顺序会影响是否安全

```java
if (choice != null && choice.equalsIgnoreCase("y")) {
    System.out.println("继续");
}
```

`&&` 的左侧为 false 时，不执行右侧，所以 choice 为 null 时不会调用它的方法。顺序反过来就失去了这层保护。

`||` 在左侧为 true 时不执行右侧。例如：

```java
if (days < 1 || days > 7) {
    System.out.println("范围错误");
}
```

运算符优先级中，比较先于 `&&`，`&&` 先于 `||`。混合条件时加括号，把业务意图直接写出来：

```java
boolean canStudy = (days >= 1 && days <= 7) && hours >= 0.5;
```

`=` 是赋值，`==` 是比较；不要用单个 `&`/`|` 替代 `&&`/`||` 来表达需要短路的输入检查。

### String 的内容比较

```java
String a = new String("y");
String b = new String("y");
System.out.println(a == b);       // false：不同对象
System.out.println(a.equals(b));  // true：内容相同
```

字符串字面量可能共享对象，导致某些 `==` 实验“恰好成功”。输入来自 Scanner 时，不应依赖这种现象。判断内容使用 equals 或 equalsIgnoreCase。

## 循环的状态：哪些值应该在每轮重置

看一段累计：

```java
double weeklyHours = 10.0;
double accumulated = 0.0;
for (int week = 1; week <= 3; week++) {
    accumulated += weeklyHours;
    System.out.println(accumulated);
}
```

| 进入循环时的 week | 累加前 | 累加后 | 下一次 week |
|---:|---:|---:|---:|
| 1 | 0 | 10 | 2 |
| 2 | 10 | 20 | 3 |
| 3 | 20 | 30 | 4，结束 |

如果把 accumulated 声明并初始化在循环体内，每次都会归零，得到 10、10、10。它应该跨本次报表的多轮循环保留，但下一次重新生成报表时应从 0 开始。

完整计划器的外层循环负责“一轮新计算”，读取方法的内层循环负责“同一字段重试”。这两个层次不能混淆：一天数输错应重读天数，而不是丢掉所有已输入内容；选择 y 则应该重新读取全部参数。

### break、continue 与 return 的范围

- break：退出当前最内层循环，继续循环后面的代码。
- continue：跳过当前循环这一轮剩余语句，for 接着执行更新表达式，while 接着检查条件。
- return：退出当前方法，将结果交给调用者；它不是“退出所有循环”的另一个拼写。

没有继续条件变化或出口的循环可能永不结束。输入重试循环必须读取新值；否则它会一直处理同一份错误输入。

## 方法、局部变量与职责边界

```java
static double calculateTotalHours(double weeklyHours, int weeks) {
    double total = weeklyHours * weeks;
    return total;
}
```

方法里的 total 只在方法作用域内有效。调用方通过返回值获得结果，不是直接读取这个局部变量：

```java
double planned = calculateTotalHours(10.0, 48);
System.out.println(planned);
```

对本例的基本类型参数，方法收到的是值的副本。修改参数不会修改调用方的变量：

```java
static int addOne(int value) {
    value = value + 1;
    return value;
}
// 在 main 中：
int original = 4;
int result = addOne(original);
// original 仍为 4，result 为 5。
```

Java 的参数传递是按值传递；对象参数以后会讲，这里先理解整数和浮点参数。

计算方法只接收有效数值并返回结果，不依赖 Scanner 或终端，因而更容易验证。读取方法处理交互，报表方法处理显示。这样的拆分也会延伸到后端的控制器、业务逻辑与数据访问。

本课的方法是为了清楚组织小程序，不要求每一行都拆成单独方法。以职责是否清楚、是否能独立解释和验证为准。

## 运行完整的交互式计划器

从当前 lessons/day-003 返回仓库根目录，再进入项目：

```bash
cd ../..
cd projects/java-foundations
mvn -B -ntp test
java -cp target/classes com.dailystudy.day003.InteractiveStudyPlanner
```

原有 第 01 课 程序仍位于自己的包中，本课新增 第 03 课 类，便于对照固定版本与交互版本。

按提示依次输入：

```text
前端开发者
4
2.5
48
n
```

应看到：

```text
每周学习小时：10.00
48 周总学习小时：480.00
符合当前节奏：保持练习、验证与复盘。
第 1 周累计：10.00 小时
第 2 周累计：20.00 小时
第 3 周累计：30.00 小时
```

程序还会显示称呼、输入提示与退出文字。输入 y 再计算一轮；n 在一轮结束后退出；任何输入阶段输入 q 可取消。输入流结束也正常退出。

### 输入规则

| 输入 | 规则 |
|---|---|
| 称呼 | 去掉首尾空白后不能为空 |
| 每周天数 | 整数，1–7 |
| 每天小时 | 有限数字，0.5–6.0，英文句点表示小数 |
| 计划周数 | 整数，1–52 |
| 继续选择 | y 或 n，忽略大小写；q 也能退出 |

这些范围是本课程程序的约定；学习节奏提示不等同于强制用户按某个投入学习。

### 阅读完整实现时的两个辅助概念

`Integer` 和 `Double` 是包装类型，此处允许读取方法返回 null 表示取消。调用方先判断是否为 null，再进入计算。本课理解这个约定即可，包装类型会在后续展开。

`printf(Locale.ROOT, "%.2f", value)` 将数字按两位小数打印。Locale.ROOT 让小数点显示不随系统区域配置变化。它控制显示格式，不改变前面计算得到的值。

::: details 展开完整计划器源码
<<< @/../projects/java-foundations/src/main/java/com/dailystudy/day003/InteractiveStudyPlanner.java
:::


## 常见问题

| 现象 | 检查方向 |
|---|---|
| 程序显示提示后停住 | 正在等输入，输入后按 Enter |
| 最小例子输入 abc 后抛异常 | 它假设合法数字；完整版演示解析失败恢复 |
| 修改源码后结果未变 | 重新编译，确认运行的是 第 03 课 完整类名 |
| 输入 2,5 不被接受 | 按程序约定使用英文句点 2.5 |
| 8 小时进入了低投入分支 | 检查 < 与 <= 的边界 |
| 循环输出少一周或多一周 | 检查初值、条件、递增次序 |
| 方法打印了数字，但调用方拿不到结果 | 检查是否有匹配的返回类型与 return |
| q 后出现 null 相关错误 | 检查是否先判断取消，再使用值或调用方法 |


## 综合练习

先独立预测并修改代码，再展开答案核对。

### A. 追踪边界（必做）

不运行，填写分类：

| 每周小时 | 预测类别 | 实际类别 |
|---:|---|---|
| 7.99 | | |
| 8.0 | | |
| 12.0 | | |
| 12.01 | | |

在 lessons/day-003 下新建 BoundaryPractice.java，使用这个外壳，自己补齐三个分支，再依次替换 weeklyHours 验证：

```java
public class BoundaryPractice {
    public static void main(String[] args) {
        double weeklyHours = 7.99;
        // 在这里补齐三类判断和输出。
    }
}
```

在同一目录执行 `javac -encoding UTF-8 -d out BoundaryPractice.java` 和 `java -cp out BoundaryPractice`。这是独立的边界实验，无需改变完整程序的输入范围。

解释：为什么把第一个条件改成 <=8.0 会改变边界？两个独立 if 与 if/else if 的执行有何不同？

::: details 展开参考解释
7.99 低于建议投入；8.0、12.0 符合；12.01 高于。第一个条件使用 <8，所以 8 会继续进入第二个分支。

独立 if 的条件都会按顺序检查，可能执行多个块；if/else if 在第一个匹配后不再检查后续分支。
:::

### B. 输入校验与恢复（必做）

运行完整计划器，至少验证：

- 天数：abc → 8 → 4。
- 小时：NaN → 6.1 → 2.5。
- 周数：0 → 53 → 48。
- 正常计算后 y，再使用一组不同参数。
- 在一个尚未完成的输入阶段输入 q。

判断每一步是否进入解析、范围检查、重试或退出。无效输入不应被当成 0，也不应产生部分报表。

::: details 展开参考解释
abc 无法转换为整数；8 转换成功但越界；4 成功。NaN 可以被浮点解析器识别，但不是有限数；6.1 超过范围；2.5 合法。0、53 是整数但周数越界。

y 开始全新一轮读取，q 取消尚未完成的输入并正常退出，不打印半份计划。完整程序也处理输入流结束。
:::

### C. 自己写方法（必做）

在最小示例 InputBasics.java 中新增：

```java
static double calculateTotalHours(double weeklyHours, int weeks) {
    // 由你完成。
}
```

在 main 中调用，使用每周 10 小时、48 周，打印返回结果。然后改为每周 8 小时、10 周。

你需要解释：参数来自哪里？返回值交给谁？为什么只有 println 而没有 return 不满足这个方法签名？

::: details 展开参考解释
```java
static double calculateTotalHours(double weeklyHours, int weeks) {
    return weeklyHours * weeks;
}
```

调用示例：

```java
double total = calculateTotalHours(10.0, 48);
System.out.println(total);
```

得到 480.0；8.0 和 10 得到 80.0。方法要求返回 double，单独打印不会为调用方提供返回值。
:::

### D. 循环累计（必做）

在最小示例中将预览改为 4 周，按每周 10 小时预测累计值。

再加入 `int plannedWeeks = 2;`，要求预览不能超过真实计划周数。最后改成 plannedWeeks=1 和 5，验证边界。不能为了两周样例直接把循环上限永久写死为 2。

::: details 展开参考解释
```java
int plannedWeeks = 2;
double accumulated = 0.0;
for (int week = 1; week <= 4 && week <= plannedWeeks; week++) {
    accumulated += weeklyHours;
    System.out.println("第 " + week + " 周累计：" + accumulated);
}
```

每周 10 小时时，四周为 10、20、30、40；实际计划两周时只有 10、20；一周只有 10；五周计划仍最多预览四周。
:::

### E. break / continue / return（扩展）

先预测下面各行输出：

```java
for (int day = 1; day <= 4; day++) {
    if (day == 2) continue;
    if (day == 4) break;
    System.out.println(day);
}
System.out.println("结束");
```

再把 break 改成 return，预测“结束”是否仍会打印。

::: details 展开参考解释
原代码依次输出 1、3、结束。day=2 跳过本轮打印；day=4 离开循环，之后继续运行循环外的打印。

在 main 中把 break 改成 return，会直接结束 main，不打印“结束”。在其他方法中则结束那个方法，不代表一律结束整个程序。
:::

### F. 输入不是对象引用（扩展）

说明为什么不使用 `choice == "y"` 判断用户读入的内容；给出正确写法，并说明若 choice 为 null 应先做什么。

Java 的 if 不能直接写 `if (days)`，请给出显式判断非零的条件。

::: details 展开参考解释
```java
if (choice != null && choice.equalsIgnoreCase("y")) {
    // 继续
}
```

&& 会短路：choice 为 null 时不再执行右侧方法调用。字符串的 == 比较引用，输入文本应比较内容。

显式非零判断为 `days != 0`。如果业务规则要求天数 1–7，还应检查完整范围，非零并不够。


:::

## 理解检查

- 从终端读取一个整数和一个小数。
- 解释文字解析失败与数值越界的区别。
- 正确分类 8 和 12 两个边界。
- 写 for 输出指定次数，解释初值、条件和更新。
- 写出有退出条件的输入循环。
- 新增一个返回计算结果的方法，并由 main 调用。
- 完整程序输错后能重试，q 能退出。

Maven 的自动测试通过说明仓库实现通过了这些检查，不自动代表你已能独立完成。本课的学习验收看自己的操作与解释。


## 延伸阅读

- [Java 官方：语言基础](https://dev.java/learn/language-basics/)：变量、运算符、控制流。
- [Java 官方：类、对象与方法](https://dev.java/learn/classes-objects/)：本课只阅读方法相关内容。
- [Java 21 Scanner API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Scanner.html)：查 nextLine、hasNextLine。
- [Java 21 Integer API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Integer.html)：查 parseInt。
- [B 站视频检索：Java Scanner、循环、方法](https://search.bilibili.com/all?keyword=Java%20Scanner%20%E5%BE%AA%E7%8E%AF%20%E6%96%B9%E6%B3%95)：检索入口，只补对应小节，不要求本课看完基础大课。


## 下一层知识

[第 04 课：请求链路与服务端状态](/lessons/request-lifecycle) 将程序执行、输入处理与 HTTP 交换放到同一条链路中，解释请求什么时候改变数据，以及刷新、重启与持久化的区别。
