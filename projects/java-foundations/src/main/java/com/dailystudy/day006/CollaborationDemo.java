package com.dailystudy.day006;

import com.dailystudy.day005.Task;

public class CollaborationDemo {
    public static void main(String[] args) {
        Task first = new Task(1, "理解接口");
        Task second = new Task(2, "练习对象协作", true);
        Task[] tasks = {first, second};

        TaskReport textReport = new TaskReport(new TextFormatter());
        TaskReport checklistReport = new TaskReport(new ChecklistFormatter());

        System.out.println("【中文状态】");
        System.out.print(textReport.render(tasks));
        System.out.println("【符号状态】");
        System.out.print(checklistReport.render(tasks));

        first.complete();
        System.out.println("【完成第一条后，重新生成】");
        System.out.print(checklistReport.render(tasks));

        try {
            first.rename("完成后直接改名");
        } catch (IllegalStateException error) {
            System.out.println("改名被拒绝：" + error.getMessage());
        }
        System.out.println("任务标题仍为：" + first.getTitle());
    }
}

// These package-private classes stay in one file to keep the teaching example small.
abstract class StatusFormatter implements TaskFormatter {
    private final String itemPrefix;

    protected StatusFormatter(String itemPrefix) {
        if (itemPrefix == null || itemPrefix.isBlank()) {
            throw new IllegalArgumentException("条目前缀不能为空。");
        }
        this.itemPrefix = itemPrefix.strip();
    }

    @Override
    public final String format(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("任务不能为空。");
        }
        // Formatting makes a single line; it does not rename the domain object.
        String singleLineTitle = task.getTitle().replace('\r', ' ').replace('\n', ' ');
        return itemPrefix + " #" + task.getId()
                + " [" + statusLabel(task.isCompleted()) + "] " + singleLineTitle;
    }

    protected abstract String statusLabel(boolean completed);
}

final class TextFormatter extends StatusFormatter {
    TextFormatter() {
        super("任务");
    }

    @Override
    protected String statusLabel(boolean completed) {
        return completed ? "完成" : "待办";
    }
}

final class ChecklistFormatter extends StatusFormatter {
    ChecklistFormatter() {
        super("TASK");
    }

    @Override
    protected String statusLabel(boolean completed) {
        return completed ? "x" : " ";
    }
}
