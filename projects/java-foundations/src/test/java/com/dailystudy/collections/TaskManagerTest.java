package com.dailystudy.collections;

import com.dailystudy.day005.Task;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaskManagerTest {
    @Test
    void storesSeparateTasksWithTheSameTitleInInsertionOrder() {
        TaskManager manager = new TaskManager();
        Task first = manager.add("  同名任务  ");
        Task second = manager.add("同名任务");
        assertEquals(1, first.getId());
        assertEquals(2, second.getId());
        assertEquals("同名任务", first.getTitle());
        assertEquals(List.of(first, second), manager.list());
        assertSame(first, manager.find(1));
        assertSame(second, manager.find(2));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t"})
    void rejectedAddDoesNotConsumeAnIdOrChangeStoredData(String title) {
        TaskManager manager = new TaskManager();
        Task existing = manager.add("已有任务");
        assertThrows(IllegalArgumentException.class, () -> manager.add(title));
        assertEquals(List.of(existing), manager.list());
        assertEquals(2, manager.add("随后新增").getId());
    }

    @Test
    void excessiveTitleIsRejectedBeforeAnIdIsConsumed() {
        TaskManager manager = new TaskManager();
        assertThrows(IllegalArgumentException.class, () -> manager.add("长".repeat(121)));
        assertTrue(manager.list().isEmpty());
        assertEquals(1, manager.add("有效标题").getId());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, 99})
    void missingQueriesAndActionsFollowTheirDeclaredContracts(int id) {
        TaskManager manager = new TaskManager();
        Task existing = manager.add("已有任务");
        assertNull(manager.find(id));
        assertFalse(manager.delete(id));
        assertThrows(IllegalArgumentException.class, () -> manager.complete(id));
        assertThrows(IllegalArgumentException.class, () -> manager.reopen(id));
        assertThrows(IllegalArgumentException.class, () -> manager.rename(id, "不应修改"));
        assertEquals(List.of(existing), manager.list());
        assertEquals("已有任务", existing.getTitle());
        assertFalse(existing.isCompleted());
    }

    @Test
    void completingAndReopeningFilterTheCurrentStateWithoutRemovingTasks() {
        TaskManager manager = new TaskManager();
        Task first = manager.add("任务一");
        Task second = manager.add("任务二");
        Task third = manager.add("任务三");
        manager.complete(second.getId());
        manager.complete(second.getId());
        assertEquals(List.of(first, third), manager.pending());
        assertEquals(List.of(first, second, third), manager.list());
        manager.reopen(second.getId());
        assertEquals(manager.list(), manager.pending());
    }

    @Test
    void renamingObeysTaskRulesAndPreservesPreviousDataOnFailure() {
        TaskManager manager = new TaskManager();
        Task task = manager.add("原标题");
        manager.rename(task.getId(), "  新标题  ");
        assertEquals("新标题", task.getTitle());
        assertThrows(IllegalArgumentException.class, () -> manager.rename(task.getId(), " "));
        assertEquals("新标题", task.getTitle());
        manager.complete(task.getId());
        assertThrows(IllegalStateException.class, () -> manager.rename(task.getId(), "不应生效"));
        assertEquals("新标题", task.getTitle());
        manager.reopen(task.getId());
        manager.rename(task.getId(), "重新打开后修改");
        assertEquals("重新打开后修改", task.getTitle());
    }

    @Test
    void deletionDoesNotRenumberSurvivorsOrReuseDeletedIds() {
        TaskManager manager = new TaskManager();
        Task first = manager.add("一");
        Task second = manager.add("二");
        Task third = manager.add("三");
        assertTrue(manager.delete(second.getId()));
        assertFalse(manager.delete(second.getId()));
        Task fourth = manager.add("四");
        assertEquals(4, fourth.getId());
        assertEquals(List.of(first, third, fourth), manager.list());
        assertSame(third, manager.find(3));
    }

    @Test
    void modifyingReturnedListStructuresDoesNotModifyTheManager() {
        TaskManager manager = new TaskManager();
        Task task = manager.add("任务");
        List<Task> listed = manager.list();
        List<Task> pending = manager.pending();
        listed.clear();
        pending.add(new Task(99, "外部条目"));
        assertEquals(List.of(task), manager.list());
        assertEquals(List.of(task), manager.pending());
        assertNull(manager.find(99));
    }

    @Test
    void listCopiesShareTaskObjectsButNotLaterStructuralChanges() {
        TaskManager manager = new TaskManager();
        Task task = manager.add("任务");
        List<Task> earlier = manager.list();
        earlier.get(0).complete();
        assertTrue(manager.find(task.getId()).isCompleted());
        manager.add("后新增的任务");
        assertEquals(1, earlier.size());
        assertEquals(2, manager.list().size());
    }

    @Test
    void managerInstancesHaveSeparateDataAndIdCounters() {
        TaskManager first = new TaskManager();
        TaskManager second = new TaskManager();
        assertEquals(1, first.add("第一份").getId());
        assertEquals(1, second.add("第二份").getId());
        first.complete(1);
        assertFalse(second.find(1).isCompleted());
    }
}
