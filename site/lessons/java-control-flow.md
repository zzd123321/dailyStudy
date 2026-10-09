---
description: 变量与类型、运算符、字符串、条件、循环、数组和方法，通过成绩统计程序练习输入、计算与返回值。
---

# Java 02 · 基本语法、数组与方法

本章先学习变量、判断和循环，再用数组保存多个值，最后把计算写成方法。综合例子是一份成绩统计：输入三次成绩，计算平均分，并统计及格次数。

知识顺序参考 [Java MOOC 的变量章节](https://github.com/rage/java-programming/blob/master/data/part-1/4-variables.md)和[方法章节](https://github.com/rage/java-programming/blob/master/data/part-2/4-methods.md)。短代码放在 main 里运行，完整程序在本章末尾。

## 变量和类型

```java
int score = 85;
double average = 82.5;
boolean passed = true;
char grade = 'B';
String name = "小林";
```

变量声明由类型、名字和初始值组成。int score 表示 score 保存整数；后面再赋值时，类型不会改变：

```java
score = 90;   // 允许。
// score = "90"; // 不允许：字符串不能赋给 int。
```

这与 TypeScript 中声明类型后的检查有相似之处，但 Java 在这里区分整数与浮点数，而不是都用 number。

| 类型 | 常见用途 | 注意点 |
|---|---|---|
| int | 数量、下标、普通整数 | 32 位有符号整数，不是无限大 |
| long | 大整数 | 字面量可写 3000000000L |
| double | 浮点计算 | 不保证十进制小数精确 |
| boolean | 条件结果 | 只有 true、false |
| char | 一个 UTF-16 代码单元 | 用单引号，不能表示所有完整 Unicode 字符 |
| String | 文本 | 用双引号，是引用类型 |

byte、short、float 是其他基本类型，先知道它们存在，遇到具体 API 再查范围。String 不是基本类型；类与引用在下一章学习。

局部变量使用前必须赋值：

```java
int total;
// System.out.println(total); // 编译失败，没有确定的初始值。
total = 0;
System.out.println(total);
```

## 运算与类型转换

### 整数除法

```java
System.out.println(10 / 4);   // 2
System.out.println(10 / 4.0); // 2.5
double value = 10 / 4;
System.out.println(value);   // 2.0
```

两个整数相除得到整数结果，向零截断。第三行先完成整数除法，再把结果转换为 double；接收变量的类型不会改变已经完成的计算。

计算平均值时，先把一个操作数转成 double：

```java
int total = 247;
int count = 3;
double average = (double) total / count;
```

数值转换与字符串解析不同：`(double) total` 转换已有数字，`Integer.parseInt("85")` 把文字解析为整数。

### 常用运算符

```java
int count = 2;
count = count + 1;
count += 1;
count++;
System.out.println(count); // 5
```

这三条语句都给 count 加一。初学时把更新语句单独写，避免在复杂表达式中同时自增。

`%` 求余数，例如 `7 % 3` 是 1，`score % 2 == 0` 可以检查是否为偶数。乘除先于加减，需要明确分组时加括号。

比较运算得到 boolean：

```java
boolean passed = score >= 60;
boolean valid = score >= 0 && score <= 100;
boolean outside = score < 0 || score > 100;
boolean failed = !passed;
```

`&&` 是且，`||` 是或，`!` 是取反。`=` 赋值，`==` 比较，不要混用。Java 的 if 条件必须是 boolean，不能直接把非零数字当 true。

基本整数可能溢出，double 可能舍入。当前 0–100 的成绩计算没有整数范围问题；金额计算会在后续学习 BigDecimal。

## 字符串

```java
String raw = "  小林  ";
String name = raw.strip();
System.out.println(name); // 小林
System.out.println(raw);  // 原字符串仍带首尾空白。
```

String 内容不可变。strip 返回处理后的字符串，不会修改 raw 原来引用的内容。`raw = raw.strip()` 是让变量引用返回结果。

### 内容比较

```java
String input = new String("yes");
System.out.println(input.equals("yes")); // true
System.out.println(input == "yes");      // false
```

比较字符串内容用 equals。`==` 对引用比较是否指向同一个对象，这里显式 new 创建的是另一个对象。不要用某次字面量比较恰好得到 true，推导出输入字符串也应该用 ==。

| 方法 | 示例 |
|---|---|
| isEmpty | `"".isEmpty()` 为 true |
| isBlank | `"  ".isBlank()` 为 true |
| strip | 去掉首尾空白 |
| equalsIgnoreCase | `"YES".equalsIgnoreCase("yes")` 为 true |
| length | 返回 UTF-16 代码单元数量 |

length 不一定等于用户看到的字符个数。这个细节先记住，真正限制文本长度时再明确计数规则。

## 条件语句

```java
int score = 85;
if (score >= 60) {
    System.out.println("及格");
} else {
    System.out.println("不及格");
}
```

条件成立执行第一个代码块，否则执行 else。多个等级按顺序检查：

```java
if (score >= 90) {
    System.out.println("优秀");
} else if (score >= 60) {
    System.out.println("及格");
} else {
    System.out.println("不及格");
}
```

只会执行第一个符合条件的分支。第二个条件不必重复写 score < 90，因为能走到这里，说明第一个条件已经不成立。

如果先判断 score >= 60，95 也会进入“及格”，后面的“优秀”没有机会执行。条件的顺序属于程序逻辑。

### 短路求值

```java
String text = null;
if (text != null && !text.isBlank()) {
    System.out.println(text);
}
```

`&&` 左边为 false 时不再执行右边，因而这里不会对 null 调用 isBlank。把顺序反过来就会在判断 null 之前调用方法。

`||` 左边为 true 时不再执行右边。后面的输入校验会用到这些规则。

### switch

根据确定值选择分支，可以使用 switch：

```java
String action = "list";
switch (action) {
    case "list" -> System.out.println("查询任务");
    case "add" -> System.out.println("新增任务");
    default -> System.out.println("未知操作");
}
```

这里使用 Java 21 支持的箭头写法，不会继续落入下一分支。区间比较仍适合使用 if。

## 循环

### for

```java
for (int i = 1; i <= 3; i++) {
    System.out.println("第 " + i + " 次");
}
```

执行过程是：初始化 i 为 1 → 检查 i <= 3 → 执行代码块 → i 加一 → 再检查条件。最后 i 变成 4，条件不成立，循环结束。

### while

```java
int remaining = 3;
while (remaining > 0) {
    System.out.println(remaining);
    remaining--;
}
```

输出 3、2、1。while 先检查条件，再执行代码块。如果删掉 remaining--，条件一直成立，程序不会正常结束。

break 结束当前循环，continue 跳过本轮剩余语句，进入下一轮：

```java
for (int number = 1; number <= 5; number++) {
    if (number == 2) continue;
    if (number == 4) break;
    System.out.println(number);
}
```

只输出 1、3。2 被跳过；遇到 4 时循环结束，5 不会处理。

## 数组

数组保存多个相同类型的值：

```java
int[] scores = {85, 70, 92};
System.out.println(scores.length); // 3
System.out.println(scores[0]);     // 85
scores[1] = 75;
```

下标从 0 开始，有效范围是 0 到 length - 1。scores[3] 越界，会在运行时抛出异常。数组创建后长度固定；需要动态增删时，下一阶段学习 List。

也可以先创建空间再写入值：

```java
int[] scores = new int[3];
scores[0] = 85;
```

int 数组的元素默认是 0，这与未赋值的局部变量不同。

### 遍历与累计

```java
int[] scores = {85, 70, 92};
int total = 0;
for (int i = 0; i < scores.length; i++) {
    total += scores[i];
}
System.out.println(total); // 247
```

`i < scores.length` 恰好覆盖 0、1、2；写成 <= 就会多访问一次。

只读取元素、不需要下标时，可以使用增强 for：

```java
for (int score : scores) {
    System.out.println(score);
}
```

这里每轮把一个元素值赋给局部变量 score。对 int 类型，给 score 重新赋值不会改写数组元素；要改数组，使用下标赋值。

## 方法、参数与返回值

把重复或独立的计算写成方法：

```java
static int sum(int[] scores) {
    int total = 0;
    for (int score : scores) {
        total += score;
    }
    return total;
}
```

这段方法放在类中、main 外面。方法不能像 JavaScript 函数那样直接定义在另一个方法内部。

int 是返回类型，sum 是方法名，scores 是参数。return 把计算结果交给调用者，并结束本次方法执行。

```java
int[] scores = {85, 70, 92};
int total = sum(scores);
System.out.println(total);
```

这里 scores 是传入的实参，方法定义中的 scores 是形参。名字恰好相同不是要求，写 `sum(values)` 也可以。

### 打印和返回

```java
static void printSum(int[] scores) {
    System.out.println(sum(scores));
}
```

void 方法不返回结果值。打印给终端看，return 给调用者使用。需要继续计算平均值时，返回 total 的方法更合适。

方法里的局部变量，只在它所在的作用域内使用。sum 内的 total 不会与 main 里同名变量变成同一份变量。

## 综合程序：成绩统计

保存为独立练习目录中的 `ScoreStats.java`。这里固定输入三次成绩，每个分数应在 0–100 之间：

```java
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class ScoreStats {
    public static void main(String[] args) {
        int[] scores = new int[3];
        try (Scanner input = new Scanner(System.in, StandardCharsets.UTF_8)) {
            for (int i = 0; i < scores.length; i++) {
                scores[i] = readScore(input, i + 1);
            }
        }
        int total = sum(scores);
        System.out.println("总分：" + total);
        System.out.println("平均分：" + (double) total / scores.length);
        System.out.println("及格次数：" + countPassed(scores));
    }

    static int readScore(Scanner input, int number) {
        while (true) {
            System.out.print("第 " + number + " 次成绩：");
            if (!input.hasNextLine()) {
                throw new IllegalStateException("输入提前结束。");
            }
            String text = input.nextLine().strip();
            try {
                int score = Integer.parseInt(text);
                if (score >= 0 && score <= 100) return score;
            } catch (NumberFormatException error) {
                // 解析失败后，下面提示重新输入。
            }
            System.out.println("请输入 0–100 之间的整数。");
        }
    }

    static int sum(int[] scores) {
        int total = 0;
        for (int score : scores) total += score;
        return total;
    }

    static int countPassed(int[] scores) {
        int count = 0;
        for (int score : scores) {
            if (score >= 60) count++;
        }
        return count;
    }
}
```

编译运行：

```bash
javac -encoding UTF-8 ScoreStats.java
java ScoreStats
```

依次输入 85、70、92，最终三行输出为：

```text
总分：247
平均分：82.33333333333333
及格次数：3
```

### 输入处理

Scanner 从 System.in 读取标准输入；nextLine 返回一行字符串，再用 parseInt 解析。键盘输入“85”，最初读到的是文本，不是整数。

读取“八十五”时，parseInt 会抛出 NumberFormatException。catch 处理这个解析错误，然后提示再次输入。输入 101 可以解析成功，但不符合 0–100 的范围，所以也重新输入。

try 中只放可能产生这里所处理错误的步骤，不是把所有失败都隐藏。输入结束时抛出的 IllegalStateException 会终止程序，本例没有把它误当成非法数字反复读取。异常的类型与传播将在专门章节学习。

`try (Scanner ...)` 是资源关闭语法。代码块结束后关闭 Scanner；本例之后不再读输入。现在先会使用，文件读写时再解释资源生命周期。

### 调试

在 for 中给 `scores[i] = readScore(...)` 设置断点。逐步执行，观察 i、scores 与返回的分数。再进入 sum，观察 total 从 0 变为 85、155、247。

断点的作用是检查程序实际执行顺序，不需要一次记住 IDE 的全部菜单。

## 练习

### 1. 平均分

预测 `double average = (85 + 70 + 92) / 3;` 的结果，再修改它得到带小数的平均值。

::: details 答案
结果是 82.0。右侧先进行整数除法。可以写 `(85 + 70 + 92) / 3.0`，或把总分转换为 double 后再除。
:::

### 2. 分支顺序

在分级判断中依次测试 59、60、89、90。为什么不能先判断 score >= 60，再判断 score >= 90？

::: details 答案
依次为不及格、及格、及格、优秀。如果先检查 >= 60，90 会被前面的分支匹配，无法到达优秀分支。
:::

### 3. 最大值

新增 `static int max(int[] scores)`，返回非空数组中的最大值。先用第一项作为初值，不要直接假定最大值为 0。

::: details 一种写法
```java
static int max(int[] scores) {
    if (scores.length == 0) throw new IllegalArgumentException("数组不能为空。");
    int largest = scores[0];
    for (int score : scores) {
        if (score > largest) largest = score;
    }
    return largest;
}
```

这里约定输入是非 null 的数组。以第一项初始化，即使以后处理全负数数组也能正确计算。完整 API 如何处理 null，将在异常章节统一讨论。
:::

### 4. 修改元素

给每个成绩加 5 分，但不超过 100。先用下标循环实现，再解释为什么给增强 for 的 int 局部变量赋值不会修改数组。

::: details 一种写法
```java
for (int i = 0; i < scores.length; i++) {
    scores[i] = Math.min(scores[i] + 5, 100);
}
```

Math.min 返回两个值中较小的值。下标赋值修改数组槽位；增强 for 中的 int score 只保存该元素值的副本。
:::

### 5. 错误输入

给第一个分数依次输入空行、abc、101、85，再输入后两次合法成绩。应该重新填写全部分数，还是仅重试当前一项？

::: details 答案
只重试当前一项。readScore 内部循环，直到本次得到合法分数才返回；外层下标在返回后才继续增加。
:::

## 对应阅读

- [MOOC：Variables](https://github.com/rage/java-programming/blob/master/data/part-1/4-variables.md)：类型、赋值与输入。
- [MOOC：Methods](https://github.com/rage/java-programming/blob/master/data/part-2/4-methods.md)：参数、返回值与方法执行过程。
- [Java 官方：变量](https://github.com/java/devjava-content/blob/main/app/pages/learn/01_tutorial/03_getting-to-know-the-language/02_basics/01_creating-variables.md)、[数组](https://github.com/java/devjava-content/blob/main/app/pages/learn/01_tutorial/03_getting-to-know-the-language/02_basics/03_creating-arrays.md)：核对声明和下标规则。
- [Javaer：类型](https://github.com/itwanger/toBeBetterJavaer/blob/master/docs/src/basic-grammar/basic-data-type.md)、[流程控制](https://github.com/itwanger/toBeBetterJavaer/blob/master/docs/src/basic-grammar/flow-control.md)：中文补充例子。

[下一章：类、对象与封装](/lessons/java-objects)。
