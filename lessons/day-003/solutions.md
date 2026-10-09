# Day 003 参考答案

## A

7.99 低于建议投入；8.0、12.0 符合；12.01 高于。第一个条件使用 <8，所以 8 会继续进入第二个分支。

独立 if 的条件都会按顺序检查，可能执行多个块；if/else if 在第一个匹配后不再检查后续分支。

## B

abc 无法转换为整数；8 转换成功但越界；4 成功。NaN 可以被浮点解析器识别，但不是有限数；6.1 超过范围；2.5 合法。0、53 是整数但周数越界。

y 开始全新一轮读取，q 取消尚未完成的输入并正常退出，不打印半份计划。完整程序也处理输入流结束。

## C

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

## D

```java
int plannedWeeks = 2;
double accumulated = 0.0;
for (int week = 1; week <= 4 && week <= plannedWeeks; week++) {
    accumulated += weeklyHours;
    System.out.println("第 " + week + " 周累计：" + accumulated);
}
```

每周 10 小时时，四周为 10、20、30、40；实际计划两周时只有 10、20；一周只有 10；五周计划仍最多预览四周。

## E

原代码依次输出 1、3、结束。day=2 跳过本轮打印；day=4 离开循环，之后继续运行循环外的打印。

在 main 中把 break 改成 return，会直接结束 main，不打印“结束”。在其他方法中则结束那个方法，不代表一律结束整个程序。

## F

```java
if (choice != null && choice.equalsIgnoreCase("y")) {
    // 继续
}
```

&& 会短路：choice 为 null 时不再执行右侧方法调用。字符串的 == 比较引用，输入文本应比较内容。

显式非零判断为 `days != 0`。如果业务规则要求天数 1–7，还应检查完整范围，非零并不够。

使用答案核对后，请脱离答案再完成一次方法和循环练习。
