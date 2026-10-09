package com.dailystudy.day005;

/** A small mutable domain object: its public operations protect task rules. */
public class Task {
    private static final int MAX_TITLE_CODE_POINTS = 120;

    private final int id;
    private String title;
    private boolean completed;

    public Task(int id, String title) {
        this(id, title, false);
    }

    public Task(int id, String title, boolean completed) {
        if (id <= 0) {
            throw new IllegalArgumentException("任务 ID 必须是正整数。");
        }
        this.id = id;
        this.title = normalizeTitle(title);
        this.completed = completed;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void rename(String title) {
        if (completed) {
            throw new IllegalStateException("已完成的任务不能修改标题，请先重新打开。");
        }
        this.title = normalizeTitle(title);
    }

    public void complete() {
        completed = true;
    }

    public void reopen() {
        completed = false;
    }

    private static String normalizeTitle(String title) {
        if (title == null) {
            throw new IllegalArgumentException("任务标题不能为空。");
        }
        String normalized = title.strip();
        if (normalized.isEmpty() ||
                normalized.codePointCount(0, normalized.length()) > MAX_TITLE_CODE_POINTS) {
            throw new IllegalArgumentException("任务标题须为非空字符串，最长 120 个 Unicode 码点。");
        }
        return normalized;
    }

    @Override
    public String toString() {
        return "Task{id=" + id + ", title='" + title + "', completed=" + completed + "}";
    }
}
