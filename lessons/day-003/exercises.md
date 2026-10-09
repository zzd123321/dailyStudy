# Day 003 练习：输入、分支、循环、方法

先预测，再运行。A–D 必做，E/F 扩展。改动前查看 git diff，保留自己已完成的练习。

## A. 追踪边界（必做）

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

## B. 输入校验与恢复（必做）

运行完整计划器，至少验证：

- 天数：abc → 8 → 4。
- 小时：NaN → 6.1 → 2.5。
- 周数：0 → 53 → 48。
- 正常计算后 y，再使用一组不同参数。
- 在一个尚未完成的输入阶段输入 q。

记录每一步是否进入解析、范围检查、重试或退出。无效输入不应被当成 0，也不应产生部分报表。

## C. 自己写方法（必做）

在最小示例 InputBasics.java 中新增：

```java
static double calculateTotalHours(double weeklyHours, int weeks) {
    // 由你完成。
}
```

在 main 中调用，使用每周 10 小时、48 周，打印返回结果。然后改为每周 8 小时、10 周。

你需要解释：参数来自哪里？返回值交给谁？为什么只有 println 而没有 return 不满足这个方法签名？

## D. 循环累计（必做）

在最小示例中将预览改为 4 周，按每周 10 小时预测累计值。

再加入 `int plannedWeeks = 2;`，要求预览不能超过真实计划周数。最后改成 plannedWeeks=1 和 5，验证边界。不能为了两周样例直接把循环上限永久写死为 2。

## E. break / continue / return（扩展）

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

## F. 输入不是对象引用（扩展）

说明为什么不使用 `choice == "y"` 判断用户读入的内容；给出正确写法，并说明若 choice 为 null 应先做什么。

Java 的 if 不能直接写 `if (days)`，请给出显式判断非零的条件。

完成后写自己的结果，再看 [参考答案](solutions.md)。
