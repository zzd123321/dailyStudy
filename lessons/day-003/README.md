# Day 003：Java 输入、判断、循环与方法

你已完成第 1 天的 Java 工具链和第 2 天的 HTTP 实验。今天回到 Java 编程，亲手把固定参数计算器升级为可交互的学习计划器。

昨天你观察了后端如何处理不同输入。今天在终端程序里实现同样的基本过程：**读取输入 → 解析类型 → 校验规则 → 计算 → 输出 → 决定是否再来一轮**。

## 1. 今天的范围与时间

| 时间 | 内容 | 产出 |
|---:|---|---|
| 10 分钟 | 回忆前两天，拉取课程 | 知道源码、字节码、输入校验的作用 |
| 20 分钟 | 类型与终端输入 | 能读取文字并转换成数字 |
| 25 分钟 | if/else 与逻辑运算 | 能校验范围并区分学习节奏 |
| 25 分钟 | for/while 与退出条件 | 能累计、重试和正常退出 |
| 20 分钟 | 方法、参数、返回值 | 能拆出独立计算逻辑 |
| 35 分钟 | 独立练习和完整程序验证 | 运行、修改并解释结果 |
| 15 分钟 | 闭卷验收、记录、Git 提交 | 留下真实操作与疑问 |

今天先形成编写小程序的能力。第 3 周还会系统展开 Java 基础；异常、包装类型和资源管理今天只认识本例需要的部分，不要求掌握整个体系。

## 2. 同步并运行最小例子

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

## 3. 类型与 TypeScript 的区别

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

## 4. 用 Scanner 读取一整行

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

### 为什么今天统一 nextLine

Scanner 的 nextInt() 读取数字 token，而 nextLine() 读取行。如果混用且不处理遗留的行尾，可能读到意外的空字符串。今天统一读整行，再显式转换，先减少这类干扰。

完整例子还会用 strip() 去掉首尾空白：输入 ` 4 ` 可以按 4 处理；名字只包含空白时则应拒绝。

源文件中的 `try (Scanner ...)` 会在结束时关闭该资源。它是资源管理语法；今天记住本 CLI 只创建一个 Scanner，退出后关闭即可。

## 5. 输入能解析，不等于符合规则

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

## 6. for：重复已知次数的操作

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

## 7. while：等待有效输入或退出

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

今天的完整程序主要使用 break 和 return；continue 留作练习理解。每次重试都必须读取新的输入，否则可能一直判断同一份错误数据。

## 8. 方法：把计算从读取和打印中拆出来

```java
static double calculateWeeklyHours(int days, double hours) {
    return days * hours;
}
```

| 部分 | 含义 |
|---|---|
| static | 今天无需创建对象即可调用这个类的方法 |
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

## 9. 处理错误解析：一次最小 try/catch

```java
try {
    int value = Integer.parseInt(text);
    // 接着检查数值范围。
} catch (NumberFormatException e) {
    System.out.println("请输入整数");
}
```

parseInt 无法把 abc 转成整数时会抛出异常。catch 处理这类预期输入错误，再让读取循环重试。转换成功的 8 仍需范围校验，不能仅靠 try/catch 解决。

今天只认识这一类恢复操作，异常体系在后面的课程展开。不要用笼统的 catch(Exception) 把所有错误悄悄忽略。

Double.parseDouble 还能解析 NaN、Infinity 等特殊值；只写范围否定判断可能漏掉 NaN。完整程序用 Double.isFinite(hours) 先确认它是有限数，再校验 0.5–6.0 的范围。

## 10. 运行完整的交互式计划器

从当前 lessons/day-003 返回仓库根目录，再进入项目：

```bash
cd ../..
cd projects/java-foundations
mvn -B -ntp test
java -cp target/classes com.dailystudy.day003.InteractiveStudyPlanner
```

原有 Day 001 程序仍位于自己的包中，今天新增 Day 003 类，便于对照固定版本与交互版本。

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

`Integer` 和 `Double` 是包装类型，此处允许读取方法返回 null 表示取消。调用方先判断是否为 null，再进入计算。今天理解这个约定即可，包装类型会在后续展开。

`printf(Locale.ROOT, "%.2f", value)` 将数字按两位小数打印。Locale.ROOT 让小数点显示不随系统区域配置变化。它控制显示格式，不改变前面计算得到的值。

## 11. 你要亲手完成的练习

做 [练习 A–D](exercises.md)，先预测再验证。尤其要亲手新增一个计算方法，不只改变样例里的数值。

有余力时做循环与字符串比较扩展。查看 [参考答案](solutions.md) 前，先在 [记录模板](worksheet.md) 留下自己的尝试。

## 12. 常见问题

| 现象 | 检查方向 |
|---|---|
| 程序显示提示后停住 | 正在等输入，输入后按 Enter |
| 最小例子输入 abc 后抛异常 | 它假设合法数字；完整版演示解析失败恢复 |
| 修改源码后结果未变 | 重新编译，确认运行的是 Day 003 完整类名 |
| 输入 2,5 不被接受 | 按程序约定使用英文句点 2.5 |
| 8 小时进入了低投入分支 | 检查 < 与 <= 的边界 |
| 循环输出少一周或多一周 | 检查初值、条件、递增次序 |
| 方法打印了数字，但调用方拿不到结果 | 检查是否有匹配的返回类型与 return |
| q 后出现 null 相关错误 | 检查是否先判断取消，再使用值或调用方法 |

## 13. 闭卷验收

- [ ] 从终端读取一个整数和一个小数。
- [ ] 解释文字解析失败与数值越界的区别。
- [ ] 正确分类 8 和 12 两个边界。
- [ ] 写 for 输出指定次数，解释初值、条件和更新。
- [ ] 写出有退出条件的输入循环。
- [ ] 新增一个返回计算结果的方法，并由 main 调用。
- [ ] 完整程序输错后能重试，q 能退出。

Maven 的自动测试通过说明仓库实现通过了这些检查，不自动代表你已能独立完成。今天的学习验收看自己的操作与解释。

## 14. 记录和提交

从仓库根目录：

```bash
cp lessons/day-003/worksheet.md notes/day-003.md
```

填写实际输入、输出、代码修改和疑问。查看差异后，只添加实际完成的文件，例如：

```bash
git status --short
git diff
git add notes/day-003.md
git add projects/java-foundations/src/main/java/com/dailystudy/day003/InteractiveStudyPlanner.java
git commit -m "study: complete day 003 Java control flow exercises"
git push origin main
```

如果在 examples/InputBasics.java 中做了练习，确认后也可以添加该文件。不要提交 out/、target/ 等生成文件。

## 15. 资料按需要查阅

- [Java 官方：语言基础](https://dev.java/learn/language-basics/)：变量、运算符、控制流。
- [Java 官方：类、对象与方法](https://dev.java/learn/classes-objects/)：今天只阅读方法相关内容。
- [Java 21 Scanner API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Scanner.html)：查 nextLine、hasNextLine。
- [Java 21 Integer API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Integer.html)：查 parseInt。
- [B 站视频检索：Java Scanner、循环、方法](https://search.bilibili.com/all?keyword=Java%20Scanner%20%E5%BE%AA%E7%8E%AF%20%E6%96%B9%E6%B3%95)：检索入口，只补对应小节，不要求今天看完基础大课。

## 16. 下一次衔接

Day 004 是第 1 周验收：把工具链、程序输入处理和 HTTP 请求连接起来，画出浏览器到后端再到数据库的请求链路，并用真实实验区分常见成功和失败。

今天完成后，反馈你能否脱离参考答案新增方法，以及最不确定的一个概念。
