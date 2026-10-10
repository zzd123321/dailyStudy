package com.dailystudy.types;

import java.util.Objects;

/** Identifies a task within one workspace, independently of its title and state. */
public final class TaskKey {
    private final int workspaceId;
    private final int taskId;

    public TaskKey(int workspaceId, int taskId) {
        if (workspaceId <= 0 || taskId <= 0) {
            throw new IllegalArgumentException("工作空间 ID 和任务 ID 必须为正数。");
        }
        this.workspaceId = workspaceId;
        this.taskId = taskId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof TaskKey otherKey)) return false;
        return workspaceId == otherKey.workspaceId && taskId == otherKey.taskId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(workspaceId, taskId);
    }

    @Override
    public String toString() {
        return workspaceId + ":" + taskId;
    }
}
