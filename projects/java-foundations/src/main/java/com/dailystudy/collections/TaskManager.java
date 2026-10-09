package com.dailystudy.collections;

import com.dailystudy.day005.Task;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** An in-memory, single-threaded manager; IDs are local to one manager instance. */
public final class TaskManager {
    private final Map<Integer, Task> tasks = new LinkedHashMap<>();
    private int nextId = 1;

    public Task add(String title) {
        Task task = new Task(nextId, title);
        tasks.put(task.getId(), task);
        nextId++;
        return task;
    }

    public Task find(int id) {
        return tasks.get(id);
    }

    public void rename(int id, String title) {
        requireTask(id).rename(title);
    }

    public void complete(int id) {
        requireTask(id).complete();
    }

    public void reopen(int id) {
        requireTask(id).reopen();
    }

    public boolean delete(int id) {
        return tasks.remove(id) != null;
    }

    /** Copies the list structure; the Task objects themselves remain shared. */
    public List<Task> list() {
        return new ArrayList<>(tasks.values());
    }

    public List<Task> pending() {
        List<Task> result = new ArrayList<>();
        for (Task task : tasks.values()) {
            if (!task.isCompleted()) {
                result.add(task);
            }
        }
        return result;
    }

    private Task requireTask(int id) {
        Task task = find(id);
        if (task == null) {
            throw new IllegalArgumentException("找不到任务：" + id);
        }
        return task;
    }
}
