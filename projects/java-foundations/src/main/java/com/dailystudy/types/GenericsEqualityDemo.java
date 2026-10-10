package com.dailystudy.types;

import com.dailystudy.collections.TaskManager;
import com.dailystudy.day005.Task;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class GenericsEqualityDemo {
    public static void main(String[] args) {
        Integer smallA = 42;
        Integer smallB = 42;
        Integer largeA = 1000;
        Integer largeB = 1000;
        System.out.println("42 的引用相同：" + (smallA == smallB));
        System.out.println("1000 的数值相同：" + largeA.equals(largeB));
        System.out.println("Integer 与 Long 相等：" + smallA.equals(42L));
        System.out.println("两个 null 相等：" + Objects.equals(null, null));

        TaskManager reading = new TaskManager();
        TaskManager coding = new TaskManager();
        Task readingTask = reading.add("阅读泛型教材");
        Task codingTask = coding.add("练习对象相等");
        System.out.println("两个管理器的首个 ID：" + readingTask.getId() + ", " + codingTask.getId());
        System.out.println("两个 Task 相等：" + readingTask.equals(codingTask));

        TaskKey original = new TaskKey(10, readingTask.getId());
        TaskKey same = new TaskKey(10, readingTask.getId());
        TaskKey anotherWorkspace = new TaskKey(20, codingTask.getId());
        System.out.println("两个键的引用相同：" + (original == same));
        System.out.println("两个键的内容相等：" + original.equals(same));
        System.out.println("相等键的 hashCode 相同：" + (original.hashCode() == same.hashCode()));

        Set<TaskKey> selected = new HashSet<>();
        selected.add(original);
        selected.add(same);
        selected.add(anotherWorkspace);
        System.out.println("选中的不同任务数：" + selected.size());

        Map<TaskKey, Task> catalog = new HashMap<>();
        catalog.put(original, readingTask);
        catalog.put(anotherWorkspace, codingTask);
        System.out.println("用新键查询：" + catalog.get(new TaskKey(10, 1)).getTitle());
        readingTask.rename("复习泛型与相等规则");
        System.out.println("改名后仍可查询：" + catalog.get(new TaskKey(10, 1)).getTitle());
        System.out.println("另一工作空间的任务：" + catalog.get(new TaskKey(20, 1)).getTitle());

        Map<String, String> collisions = new HashMap<>();
        collisions.put("Aa", "第一项");
        collisions.put("BB", "第二项");
        System.out.println("Aa 与 BB 的 hashCode 相同：" + ("Aa".hashCode() == "BB".hashCode()));
        System.out.println("碰撞后的条目数：" + collisions.size());
        System.out.println("分别查询：" + collisions.get("Aa") + ", " + collisions.get("BB"));
    }
}
