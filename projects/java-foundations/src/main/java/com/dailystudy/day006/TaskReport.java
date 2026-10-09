package com.dailystudy.day006;

import com.dailystudy.day005.Task;

public final class TaskReport {
    private final TaskFormatter formatter;

    public TaskReport(TaskFormatter formatter) {
        if (formatter == null) {
            throw new IllegalArgumentException("格式化器不能为空。");
        }
        this.formatter = formatter;
    }

    public String render(Task[] tasks) {
        if (tasks == null) {
            throw new IllegalArgumentException("任务数组不能为空引用。");
        }
        // Check the whole input before asking a collaborator to do any work.
        for (Task task : tasks) {
            if (task == null) {
                throw new IllegalArgumentException("任务数组不能包含空任务。");
            }
        }

        StringBuilder report = new StringBuilder("任务清单\n");
        if (tasks.length == 0) {
            return report.append("（暂无任务）\n").toString();
        }
        for (Task task : tasks) {
            report.append(formatter.format(task)).append('\n');
        }
        return report.toString();
    }
}
