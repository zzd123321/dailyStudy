package com.dailystudy.collections;

import com.dailystudy.day005.Task;

import java.util.List;

public class TaskManagerDemo {
    public static void main(String[] args) {
        TaskManager manager = new TaskManager();
        Task first = manager.add("阅读集合教材");
        Task second = manager.add("练习 Map 查询");
        Task third = manager.add("整理示例代码");
        printTasks("初始任务", manager.list());

        manager.rename(second.getId(), "练习按 ID 查询");
        manager.complete(first.getId());
        printTasks("未完成任务", manager.pending());
        System.out.println("查询 ID 2：" + manager.find(second.getId()).getTitle());
        System.out.println("查询 ID 99：" + manager.find(99));

        try {
            manager.rename(first.getId(), "完成后改名");
        } catch (IllegalStateException error) {
            System.out.println("改名被拒绝：" + error.getMessage());
        }

        System.out.println("删除 ID 2：" + manager.delete(second.getId()));
        System.out.println("再次删除 ID 2：" + manager.delete(second.getId()));
        Task fourth = manager.add("练习删除后的新增");
        System.out.println("新增任务的 ID：" + fourth.getId());
        printTasks("删除并新增后", manager.list());

        List<Task> returned = manager.list();
        returned.clear();
        System.out.println("清空返回列表后，管理器任务数：" + manager.list().size());
        third.complete();
        System.out.println("通过任务引用完成 ID 3：" + manager.find(third.getId()).isCompleted());
    }

    private static void printTasks(String heading, List<Task> tasks) {
        System.out.println("【" + heading + "】");
        for (Task task : tasks) {
            String status = task.isCompleted() ? "完成" : "待办";
            System.out.println(task.getId() + " | " + status + " | " + task.getTitle());
        }
    }
}
