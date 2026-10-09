package com.dailystudy.day005;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {
    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void constructionRejectsNonPositiveIds(int id) {
        assertThrows(IllegalArgumentException.class, () -> new Task(id, "合法标题"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t\n"})
    void constructionRejectsMissingTitles(String title) {
        assertThrows(IllegalArgumentException.class, () -> new Task(1, title));
    }

    @Test
    void titleLengthCountsCodePointsAndIgnoresOuterWhitespace() {
        String boundary = "🧠".repeat(120);
        Task task = new Task(1, "  " + boundary + "  ");
        assertEquals(boundary, task.getTitle());
        assertThrows(IllegalArgumentException.class, () -> new Task(2, boundary + "🧠"));
    }

    @Test
    void completingOneTaskDoesNotChangeAnotherAndMayBeRepeated() {
        Task first = new Task(1, "任务一");
        Task second = new Task(2, "任务二");
        assertFalse(first.isCompleted());
        first.complete();
        first.complete();
        assertTrue(first.isCompleted());
        assertFalse(second.isCompleted());
    }

    @Test
    void completedTaskRejectsRenameWithoutChangingState() {
        Task task = new Task(1, "原标题");
        task.complete();
        assertThrows(IllegalStateException.class, () -> task.rename("新标题"));
        assertEquals("原标题", task.getTitle());
        assertTrue(task.isCompleted());
    }

    @Test
    void reopeningAllowsRenameAndPreservesIdentity() {
        Task task = new Task(7, "原标题", true);
        task.reopen();
        task.reopen();
        task.rename("  新标题  ");
        assertEquals(7, task.getId());
        assertEquals("新标题", task.getTitle());
        assertFalse(task.isCompleted());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void invalidRenameLeavesThePreviousTitleIntact(String title) {
        Task task = new Task(1, "原标题");
        assertThrows(IllegalArgumentException.class, () -> task.rename(title));
        assertEquals("原标题", task.getTitle());
        assertFalse(task.isCompleted());
    }

    @Test
    void excessiveRenameDoesNotDiscardAValidTitle() {
        Task task = new Task(1, "原标题");
        assertThrows(IllegalArgumentException.class, () -> task.rename("长".repeat(121)));
        assertEquals("原标题", task.getTitle());
        task.rename("恢复正常修改");
        assertEquals("恢复正常修改", task.getTitle());
    }

    @Test
    void explicitInitialStateObeysTheSameOperationRules() {
        Task task = new Task(3, "已完成任务", true);
        assertTrue(task.isCompleted());
        assertThrows(IllegalStateException.class, () -> task.rename("不应生效"));
        task.reopen();
        task.rename("允许修改");
        assertEquals("允许修改", task.getTitle());
    }
}
