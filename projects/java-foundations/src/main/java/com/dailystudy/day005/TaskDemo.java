package com.dailystudy.day005;

/** Day 005: construction, encapsulation, independent state, and reference values. */
public class TaskDemo {
    public static void main(String[] args) {
        Task first = new Task(1, "  理解类与对象  ");
        Task second = new Task(2, "练习构造器");
        System.out.println("初始任务一：" + first);
        System.out.println("初始任务二：" + second);

        first.rename("理解封装");
        first.complete();
        System.out.println("完成后：" + first);
        System.out.println("任务二仍未完成：" + !second.isCompleted());

        try {
            first.rename("不应生效的标题");
        } catch (IllegalStateException error) {
            System.out.println("改名被拒绝：" + error.getMessage());
        }
        System.out.println("失败后标题：" + first.getTitle());

        first.reopen();
        first.rename("练习引用传递");
        System.out.println("重新打开后：" + first);

        Task alias = second;
        completeTask(alias);
        System.out.println("别名指向同一对象：" + (alias == second));
        System.out.println("通过别名完成后任务二：" + second);

        Task another = new Task(2, "练习构造器");
        System.out.println("相同 ID 等于同一对象吗：" + (another == second));

        Task restored = new Task(3, "已完成的示例任务", true);
        System.out.println("指定初始状态：" + restored);
    }

    private static void completeTask(Task task) {
        task.complete();
    }
}
