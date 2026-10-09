package com.dailystudy.day006;

import com.dailystudy.day005.Task;

/** Reads one task and returns one line of plain text without changing the task. */
public interface TaskFormatter {
    String format(Task task);
}
