---
description: List、ArrayList、Set、HashSet、Map 与 LinkedHashMap 的基本用法，以及按 ID 管理任务、过滤、删除和集合复制。
---

# Java 05 · 集合与任务管理

Java 02 用数组保存三次成绩。数组长度固定，适合数量已经确定的数据；任务管理器却要随时新增和删除，使用集合更方便。

本章先学习 List、Set 和 Map，再使用 Java 03 的 Task 建立内存任务管理器。教材参考 [MOOC：Lists](https://github.com/rage/java-programming/blob/master/data/part-3/2-lists.md)与 [Hash map](https://github.com/rage/java-programming/blob/master/data/part-8/2-hash-map.md)，中文补充使用 [Javaer 集合概览](https://github.com/itwanger/toBeBetterJavaer/blob/master/docs/src/collection/gailan.md)。示例采用 Java 21。

## 集合的三种常见用途

| 要保存的数据 | 适合的类型 | 主要特点 |
|---|---|---|
| 按顺序排列的任务、成绩、消息 | List | 有顺序、可重复、可按下标访问 |
| 不重复的标签、角色名 | Set | 元素不重复，不使用列表下标 |
| ID 对应任务、用户名对应用户 | Map | 每个键最多对应一个值，按键查询 |

这里的“有顺序”是元素在容器中的排列，不等于自动排序。List 不会自动把成绩从小到大排列。

List 和 Set 继承 Collection 接口；Map 是另一套接口，不是 Collection 的子接口。先记住它们解决的问题，不需要一次背下全部集合继承关系。

## List 与 ArrayList

List 是接口，ArrayList 是常用实现：

```java
List<String> titles = new ArrayList<>();
titles.add("阅读教材");
titles.add("完成练习");
titles.add("阅读教材");
System.out.println(titles); // [阅读教材, 完成练习, 阅读教材]
```

短代码需要导入相应类型：

```java
import java.util.List;
import java.util.ArrayList;
```

左边使用接口类型，右边创建具体实现，与上一章的接口赋值相同。`<String>` 表示列表中的元素是字符串，不能往这个列表添加 Task。右侧 `<>` 让编译器推断类型。

泛型完整规则在下一章学习。现在先用带元素类型的集合，不使用省略类型参数的原始类型。

### 新增、读取、替换与删除

```java
List<String> titles = new ArrayList<>();
titles.add("阅读教材");
titles.add("完成练习");

System.out.println(titles.size()); // 2
System.out.println(titles.get(0)); // 阅读教材

titles.set(1, "整理笔记");
titles.add(1, "运行示例");
System.out.println(titles); // [阅读教材, 运行示例, 整理笔记]

titles.remove(0);
System.out.println(titles); // [运行示例, 整理笔记]
```

get 读取元素，set 替换已有位置，add(index, value) 在指定位置插入。删除后，后面的元素下标会向前移动。

读取、替换和按下标删除要求 `0 <= index < size()`。指定位置插入允许 index 等于 size，表示放到末尾。访问不存在的下标会抛出 IndexOutOfBoundsException。

| 对象 | 查询长度或数量 |
|---|---|
| 数组 | array.length |
| String | text.length() |
| List / Set / Map | collection.size() |

isEmpty 检查当前是否没有元素；clear 清空容器；contains 检查是否存在指定元素。对 String，contains 按 equals 比较内容。

### 数字列表的删除重载

```java
List<Integer> numbers = new ArrayList<>(List.of(10, 20, 10));
numbers.remove(1);
System.out.println(numbers); // [10, 10]
numbers.remove(Integer.valueOf(10));
System.out.println(numbers); // [10]
```

remove(1) 选择 remove(int index)，删除下标 1 的 20。Integer.valueOf(10) 明确提供对象参数，选择按值删除，只删除第一个相等元素。

泛型不能写 `<int>`，要写包装类型 Integer。add(10) 时编译器可以进行自动装箱，把 int 转成 Integer；完整规则留到下一章。

### size 与容量

```java
List<String> titles = new ArrayList<>(10);
System.out.println(titles.size()); // 0
```

10 是初始容量建议，不是十个已经存在的元素。不能因此读取 get(0)。ArrayList 内部使用数组，需要时扩容；普通程序先使用无参构造器即可。

ArrayList 的下标读取通常很快，中间插入或删除需要移动后面的元素。现在先按用途选它，不把集合选择简化成“某个实现永远最快”。

## 遍历与过滤

读取全部标题可以使用增强 for：

```java
for (String title : titles) {
    System.out.println(title);
}
```

需要下标时使用普通循环：

```java
for (int i = 0; i < titles.size(); i++) {
    System.out.println(i + "：" + titles.get(i));
}
```

过滤时，先建一个结果列表，将符合条件的元素加入它：

```java
List<String> titles = new ArrayList<>(List.of("阅读教材", "", "完成练习"));
List<String> nonEmpty = new ArrayList<>();
for (String title : titles) {
    if (!title.isBlank()) nonEmpty.add(title);
}
System.out.println(nonEmpty); // [阅读教材, 完成练习]
System.out.println(titles.size()); // 3，原列表未删元素。
```

这里约定列表中没有 null 字符串。过滤得到新的列表，不自动修改原列表。

### 遍历期间删除

不要在增强 for 中直接对原列表 add 或 remove。这种结构修改可能使迭代失效并抛出 ConcurrentModificationException；是否抛出异常也不能作为程序正确性的保证。

按下标原地删除时，可以从后向前：

```java
List<String> titles = new ArrayList<>(List.of("", "阅读教材", "", ""));
for (int i = titles.size() - 1; i >= 0; i--) {
    if (titles.get(i).isBlank()) titles.remove(i);
}
System.out.println(titles); // [阅读教材]
```

删除位置右边的元素会前移，但左边尚未处理的元素下标不变，因此不会跳过相邻空标题。

以后还会学习 Iterator.remove、removeIf 和 Stream。当前能解释这个循环与新结果列表两种写法即可。

## Set 与 HashSet

用 Set 保存不重复的角色名：

```java
Set<String> roles = new HashSet<>();
System.out.println(roles.add("reader")); // true
System.out.println(roles.add("editor")); // true
System.out.println(roles.add("reader")); // false
System.out.println(roles.size());        // 2
System.out.println(roles.contains("editor")); // true
```

需要导入 java.util.Set 与 java.util.HashSet。重复添加不会再多出一项，add 返回值告诉你本次是否新增成功。

HashSet 不保证遍历顺序。如果需要保留首次插入顺序，可以使用 LinkedHashSet；若需要按比较规则排序，则是另一种需求。

“不重复”依据元素的相等规则。对 String 是内容相等；对当前 Task，默认 equals 仍按对象身份判断。两次 new 出来的同 ID 任务不会自动被 Set 合并，这将在下一章学习 equals 与 hashCode 时验证。

## Map 与 HashMap

Map 存储键和值：

```java
Map<Integer, String> titlesById = new HashMap<>();
titlesById.put(7, "阅读教材");
titlesById.put(20, "完成练习");
System.out.println(titlesById.get(20)); // 完成练习
System.out.println(titlesById.get(99)); // null
System.out.println(titlesById.containsKey(7)); // true
```

需要导入 java.util.Map 与 java.util.HashMap。Integer 是键的类型，String 是值的类型；两个泛型参数描述不同角色。

Map 的 20 是键，不是第二十一项的下标。键可以不连续，删除键 7 不会把键 20 改成 19。

### 同一个键会替换值

```java
String previous = titlesById.put(20, "整理笔记");
System.out.println(previous); // 完成练习
System.out.println(titlesById.size()); // 2
System.out.println(titlesById.get(20)); // 整理笔记
```

同一个键最多对应一个值。put 返回旧值，没有旧映射时返回 null。这是替换，不是同一个键挂上两条记录。

HashMap 允许 null 值，所以一般不能仅凭 get 返回 null 就断定键不存在。需要区分时使用 containsKey。本章任务管理器不保存 null 任务，因而能把 null 约定为未找到。

remove(key) 删除映射并返回原值，没有映射时返回 null。按多个对象属性查询则不是简单 get，需要遍历或另外建立索引。

### 遍历键、值或键值对

```java
for (Integer id : titlesById.keySet()) {
    System.out.println(id);
}
for (String title : titlesById.values()) {
    System.out.println(title);
}
for (Map.Entry<Integer, String> entry : titlesById.entrySet()) {
    System.out.println(entry.getKey() + "：" + entry.getValue());
}
```

keySet 是键视图，values 是值视图，entrySet 是键值对视图。Map.Entry 表示一对键和值。

这些是底层 Map 的视图，不是独立复制。不要把它们直接作为允许调用方随意修改的内部数据暴露出去。

HashMap 不保证遍历顺序。某次输出看起来按数字排列，也不是它的顺序承诺。按键查找通常无需从第一项逐个查起，平均时间复杂度可视为 O(1)，但不是所有情况都保证常数时间；散列与冲突处理以后再读。

### LinkedHashMap

默认的 LinkedHashMap 保留插入顺序，同时提供 Map 的按键查询：

```java
Map<Integer, String> titlesById = new LinkedHashMap<>();
titlesById.put(7, "第一条");
titlesById.put(2, "第二条");
System.out.println(titlesById.keySet()); // [7, 2]
titlesById.put(7, "改名后");
System.out.println(titlesById.keySet()); // [7, 2]
```

这里使用默认构造器，替换已有键的值不会把它移到末尾。LinkedHashMap 也有按访问顺序工作的构造方式，本章不使用那种模式。

任务管理器采用它，是因为我们希望列表保持新增顺序，而不是从 HashMap 某次碰巧的输出推导排列。

## 一个完整集合小程序

保存为 `CollectionsIntro.java`，可独立运行：

```java
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CollectionsIntro {
    public static void main(String[] args) {
        List<String> languages = new ArrayList<>();
        languages.add("Java");
        languages.add("Python");
        languages.add("Java");
        System.out.println("列表：" + languages);
        System.out.println("下标 1：" + languages.get(1));

        Set<String> unique = new HashSet<>(languages);
        System.out.println("不同语言数：" + unique.size());
        System.out.println("再加入 Java：" + unique.add("Java"));

        Map<Integer, String> names = new HashMap<>();
        names.put(7, "小林");
        names.put(20, "小陈");
        names.put(7, "林同学");
        System.out.println("键 7：" + names.get(7));
        System.out.println("映射数量：" + names.size());
        System.out.println("键 99：" + names.get(99));
    }
}
```

编译运行：

```bash
javac -encoding UTF-8 CollectionsIntro.java
java CollectionsIntro
```

输出：

```text
列表：[Java, Python, Java]
下标 1：Python
不同语言数：2
再加入 Java：false
键 7：林同学
映射数量：2
键 99：null
```

这个程序没有依赖 HashSet 或 HashMap 的遍历顺序，因此输出可直接对应上面结果。

## 用 Map 管理多条任务

Task 负责一条任务的标题、状态与修改规则。TaskManager 增加这些职责：分配 ID、保存多条任务、按 ID 查询与删除、生成未完成任务列表。

数据结构只有一份 `Map<Integer, Task>`。没有同时维护一份 List 和一份 Map，避免新增或删除时还要同步两份容器。

| 方法 | 约定 |
|---|---|
| add(title) | 创建任务，返回新 Task |
| find(id) | 返回任务，未找到返回 null |
| rename / complete / reopen | 未找到时抛出 IllegalArgumentException，找到后委托 Task |
| delete(id) | 删除成功返回 true，未找到返回 false |
| list() | 返回按新增顺序排列的新 List |
| pending() | 返回当前未完成任务组成的新 List |

这些是本例选择的 API 行为，不是所有查询方法都必须返回 null。Optional 与异常的其他用法后续再比较。

### TaskManager.java

源码放在 `projects/java-foundations/src/main/java/com/dailystudy/collections/`，继续引用历史包路径中的 day005.Task。

<<< ../../projects/java-foundations/src/main/java/com/dailystudy/collections/TaskManager.java{java}

### 新增的执行顺序

add 先调用 Task 构造器。标题不合法时，构造器抛出异常，后面的 put 和 nextId++ 不会执行；没有新增数据，也没有占用一个 ID。

有效新增后，下一个 ID 加一。删除任务不减小计数器，也不重新给剩余任务编号。ID 表示任务身份，不能因为它在列表中的位置改变就跟着改变。

这是单个管理器实例的内存计数。同一进程创建两份管理器，各自从 1 开始；重启后也会重新开始。跨进程保存和全局 ID 不由这个示例实现。

### 状态修改与过滤

complete、rename 和 reopen 先用 requireTask 找到目标，再调用 Task 的已有方法。完成后禁止改名的规则仍放在 Task，没有在管理器中再复制一份。

pending 遍历所有任务，只把未完成的引用加入结果列表。它没有删除已经完成的任务，也不修改任何任务状态。

## 集合复制与对象引用

list 返回：

```java
return new ArrayList<>(tasks.values());
```

它复制容器中的引用，得到新的列表结构。清空这个返回列表，不会清空管理器的 Map：

```java
List<Task> returned = manager.list();
returned.clear();
System.out.println(manager.list().size()); // 管理器中的数量未变。
```

但 Task 对象仍然共享：

```java
List<Task> returned = manager.list();
returned.get(0).complete();
```

这会改变管理器中对应 Task 的状态。可以画成：

```text
Map 的值 ──────→ Task 对象
返回 List[0] ──→ 同一个 Task 对象
```

复制集合结构不等于深复制所有元素，也不等于获得不可变数据。新加入管理器的任务不会自动出现在旧返回列表中，但旧列表已有的 Task 引用仍能读到对象状态变化。

### List.of 与可修改列表

```java
List<String> fixed = List.of("Java", "Python");
// fixed.add("SQL"); // 运行时不支持，会抛出异常。
List<String> editable = new ArrayList<>(fixed);
editable.add("SQL");
```

List.of 返回不可修改的列表，不支持增删和替换元素，且不允许 null 元素。new ArrayList<>(fixed) 则建立可修改的结构副本。

即使一个列表结构不可修改，其中元素若是可变 Task，也仍可能改变自己的状态。不可修改容器与不可变对象是两个问题。

## 运行任务管理示例

### TaskManagerDemo.java

<<< ../../projects/java-foundations/src/main/java/com/dailystudy/collections/TaskManagerDemo.java{java}

从仓库根目录执行：

```bash
mvn -B -ntp -f projects/java-foundations/pom.xml test
java -cp projects/java-foundations/target/classes com.dailystudy.collections.TaskManagerDemo
```

预期输出：

```text
【初始任务】
1 | 待办 | 阅读集合教材
2 | 待办 | 练习 Map 查询
3 | 待办 | 整理示例代码
【未完成任务】
2 | 待办 | 练习按 ID 查询
3 | 待办 | 整理示例代码
查询 ID 2：练习按 ID 查询
查询 ID 99：null
改名被拒绝：已完成的任务不能修改标题，请先重新打开。
删除 ID 2：true
再次删除 ID 2：false
新增任务的 ID：4
【删除并新增后】
1 | 完成 | 阅读集合教材
3 | 待办 | 整理示例代码
4 | 待办 | 练习删除后的新增
清空返回列表后，管理器任务数：3
通过任务引用完成 ID 3：true
```

查询 ID 99 后只打印 null，没有对它调用方法。自己的代码在使用 find 的结果前也要处理未找到情况，否则会产生 NullPointerException。

这个演示在 main 中按顺序执行操作。下一步可以自行加入菜单与 Scanner，但集合的行为不依赖交互界面；先把以上结果解释清楚。

## 练习

### 1. 下标与 ID

创建三条任务，删除第二条后，新列表的下标 1 指向谁？find(2) 与 find(3) 分别返回什么？

::: details 答案
新列表下标 1 指向原来的第三条任务，ID 仍为 3。find(2) 返回 null，find(3) 返回第三条 Task。排列位置变化不修改 ID。
:::

### 2. 不合法的新增

先新增一条合法任务，再新增空白标题，然后再新增合法任务。成功返回的 ID 应该是 1、2 还是 1、3？

::: details 答案
是 1、2。Task 构造失败时 add 还没有修改 Map 和 nextId。可以捕获异常后继续新增，再查询列表数量进行验证。
:::

### 3. 找已完成任务

参考 pending，增加 `List<Task> completed()`。如果没有已完成任务，应该返回 null 还是空列表？

::: details 参考写法
在 TaskManager 中加入：

```java
public List<Task> completed() {
    List<Task> result = new ArrayList<>();
    for (Task task : tasks.values()) {
        if (task.isCompleted()) result.add(task);
    }
    return result;
}
```

返回空列表，调用方仍能正常遍历。这里“查询结果没有元素”和“没有结果容器”不需要合并成 null。
:::

### 4. 统计不同标题

从 manager.list 读取标题，加入 `Set<String>`，得到不同标题的数量。两条同标题任务是否因此从管理器删除？

::: details 参考写法
```java
Set<String> titles = new HashSet<>();
for (Task task : manager.list()) titles.add(task.getTitle());
System.out.println(titles.size());
```

Set 只是另一个统计容器，没有删除任务。不同 Task 可以拥有相同标题；任务是否相同不能仅由标题决定。
:::

### 5. 复制的含义

保存 `List<Task> earlier = manager.list()`，再向管理器新增任务，然后修改 earlier 中已有 Task 的状态。分别预测 earlier.size 与 manager 中的状态。

::: details 答案
earlier 的结构不自动加入新任务，所以 size 不变。已有 Task 仍是同一对象，通过 earlier 修改它的状态，管理器中相应对象也改变。
:::

### 6. 删除连续元素

在字符串列表 `["", "", "阅读教材", ""]` 中删除所有空字符串。先尝试从前向后删，再与本章倒序循环比较。

::: details 解释
从前向后删除后，后面的元素向前移动；如果下标立刻加一，原来相邻的空串可能被跳过。倒序删除不会改变左边尚未处理元素的位置，结果只剩阅读教材。
:::

### 7. Map 的空值

执行 `map.put(7, null)`，再比较 get(7)、get(99) 与两次 containsKey。为什么有时不能仅用 get 判断键是否存在？

::: details 答案
两次 get 都返回 null，containsKey(7) 是 true，containsKey(99) 是 false。本例任务管理器没有存 null Task，所以 find 的 null 可以代表未找到；普通 Map 需要按自己的值约定判断。
:::

## 对应阅读

- [MOOC：Lists](https://github.com/rage/java-programming/blob/master/data/part-3/2-lists.md)：新增、下标、循环、删除，以及列表作为方法参数。
- [MOOC：Hash map](https://github.com/rage/java-programming/blob/master/data/part-8/2-hash-map.md)：键对应一个值、对象作为值、键与值的遍历。
- [Javaer：ArrayList](https://github.com/itwanger/toBeBetterJavaer/blob/master/docs/src/collection/arraylist.md)、[集合概览](https://github.com/itwanger/toBeBetterJavaer/blob/master/docs/src/collection/gailan.md)：先读基本用法，容量不等于 size。
- [Javaer：LinkedHashMap](https://github.com/itwanger/toBeBetterJavaer/blob/master/docs/src/collection/linkedhashmap.md)：比较有无顺序保证；文章中某次 HashMap 输出顺序不视为规范承诺。
- [Java 21：List API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/List.html)、[Map API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Map.html)：按需查 remove 的重载、put 返回值和视图方法。
- [OpenJDK 21：LinkedHashMap 文档与源码](https://github.com/openjdk/jdk21u/blob/master/src/java.base/share/classes/java/util/LinkedHashMap.java)：文件开头的说明用于核对默认插入顺序，暂时不用通读实现。

下一章：[泛型、包装类型与对象相等](/lessons/java-generics-equality)，继续解释集合怎样检查类型、判断重复。完整安排见[学习路线](/learning-path)。
