package com.dailystudy.types;

import com.dailystudy.collections.TaskManager;
import com.dailystudy.day005.Task;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TaskKeyTest {
    @Test
    void equalKeysAreReflexiveSymmetricTransitiveAndHaveEqualHashes() {
        TaskKey first = new TaskKey(10, 1);
        TaskKey second = new TaskKey(10, 1);
        TaskKey third = new TaskKey(10, 1);
        assertNotSame(first, second);
        assertEquals(first, first);
        assertEquals(first, second);
        assertEquals(second, first);
        assertEquals(second, third);
        assertEquals(first, third);
        assertEquals(first.hashCode(), second.hashCode());
        assertFalse(first.equals(null));
        assertFalse(first.equals("10:1"));
    }

    @Test
    void workspaceAndTaskIdBothParticipateInIdentity() {
        TaskKey key = new TaskKey(10, 1);
        assertNotEquals(key, new TaskKey(20, 1));
        assertNotEquals(key, new TaskKey(10, 2));
    }

    @ParameterizedTest
    @CsvSource({"0, 1", "-1, 1", "10, 0", "10, -1"})
    void nonPositiveIdsAreRejected(int workspaceId, int taskId) {
        assertThrows(IllegalArgumentException.class, () -> new TaskKey(workspaceId, taskId));
    }

    @Test
    void setDeduplicatesEqualKeysWhileKeepingDifferentWorkspaces() {
        Set<TaskKey> keys = new HashSet<>();
        assertTrue(keys.add(new TaskKey(10, 1)));
        assertFalse(keys.add(new TaskKey(10, 1)));
        assertTrue(keys.add(new TaskKey(20, 1)));
        assertEquals(2, keys.size());
    }

    @Test
    void freshEqualKeyCanQueryReplaceAndRemoveTheSameEntry() {
        Map<TaskKey, String> tasks = new HashMap<>();
        tasks.put(new TaskKey(10, 1), "旧标题");
        assertEquals("旧标题", tasks.get(new TaskKey(10, 1)));
        assertEquals("旧标题", tasks.put(new TaskKey(10, 1), "新标题"));
        assertEquals(1, tasks.size());
        assertEquals("新标题", tasks.remove(new TaskKey(10, 1)));
        assertTrue(tasks.isEmpty());
    }

    @Test
    void collisionDoesNotMergeDifferentTaskKeys() {
        // Objects.hash(1, 32) and Objects.hash(2, 1) collide, but the keys differ.
        TaskKey first = new TaskKey(1, 32);
        TaskKey second = new TaskKey(2, 1);
        assertEquals(first.hashCode(), second.hashCode());
        assertNotEquals(first, second);
        Map<TaskKey, String> tasks = new HashMap<>();
        tasks.put(first, "一");
        tasks.put(second, "二");
        assertEquals(2, tasks.size());
        assertEquals("一", tasks.get(new TaskKey(1, 32)));
        assertEquals("二", tasks.get(new TaskKey(2, 1)));
    }

    @Test
    void separateManagersCanUseLocalIdsWithoutCatalogOverwriting() {
        TaskManager first = new TaskManager();
        TaskManager second = new TaskManager();
        Task one = first.add("第一份任务");
        Task two = second.add("第二份任务");
        assertEquals(one.getId(), two.getId());
        Map<TaskKey, Task> catalog = new HashMap<>();
        catalog.put(new TaskKey(10, one.getId()), one);
        catalog.put(new TaskKey(20, two.getId()), two);
        first.rename(one.getId(), "改名后");
        first.complete(one.getId());
        assertEquals(2, catalog.size());
        assertSame(one, catalog.get(new TaskKey(10, 1)));
        assertEquals("改名后", catalog.get(new TaskKey(10, 1)).getTitle());
        assertTrue(catalog.get(new TaskKey(10, 1)).isCompleted());
        assertSame(two, catalog.get(new TaskKey(20, 1)));
        assertFalse(catalog.get(new TaskKey(20, 1)).isCompleted());
    }
}
