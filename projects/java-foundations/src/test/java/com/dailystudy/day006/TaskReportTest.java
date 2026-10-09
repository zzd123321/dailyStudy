package com.dailystudy.day006;

import com.dailystudy.day005.Task;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class TaskReportTest {
    @Test
    void formatsBothStatesInArrayOrderWithoutMutatingTasks() {
        Task pending = new Task(7, "待办条目");
        Task completed = new Task(2, "已完成条目", true);
        Task[] tasks = {pending, completed};
        assertEquals("任务清单\n任务 #7 [待办] 待办条目\n任务 #2 [完成] 已完成条目\n",
                new TaskReport(new TextFormatter()).render(tasks));
        assertEquals("任务清单\nTASK #7 [ ] 待办条目\nTASK #2 [x] 已完成条目\n",
                new TaskReport(new ChecklistFormatter()).render(tasks));
        assertAll(() -> assertFalse(pending.isCompleted()),
                () -> assertTrue(completed.isCompleted()),
                () -> assertEquals("待办条目", pending.getTitle()));
    }

    @Test
    void rendersCurrentStateWithoutCachingAndKeepsDomainRules() {
        Task task = new Task(1, "原标题");
        Task[] tasks = {task};
        TaskReport report = new TaskReport(new TextFormatter());
        String before = report.render(tasks);
        task.complete();
        assertTrue(report.render(tasks).contains("[完成] 原标题"));
        assertTrue(before.contains("[待办] 原标题"));
        assertThrows(IllegalStateException.class, () -> task.rename("不应生效"));
        task.reopen();
        task.rename("新标题");
        assertTrue(report.render(tasks).contains("[待办] 新标题"));
    }

    @Test
    void acceptsAnIndependentImplementationWithoutKnowingItsConcreteType() {
        TaskFormatter formatter = new IdOnlyFormatter();
        TaskReport report = new TaskReport(formatter);
        assertEquals("任务清单\nID=5\nID=1\n",
                report.render(new Task[]{new Task(5, "任务五"), new Task(1, "任务一")}));
    }

    @Test
    void emptyInputDoesNotInvokeTheFormatter() {
        TaskFormatter formatter = new RejectInvocationFormatter();
        assertEquals("任务清单\n（暂无任务）\n", new TaskReport(formatter).render(new Task[0]));
    }

    @Test
    void validatesAllInputBeforeInvokingTheFormatter() {
        TaskReport report = new TaskReport(new RejectInvocationFormatter());
        assertThrows(IllegalArgumentException.class, () -> report.render(null));
        assertThrows(IllegalArgumentException.class,
                () -> report.render(new Task[]{new Task(1, "有效条目"), null}));
        assertThrows(IllegalArgumentException.class, () -> new TaskReport(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"第一行\n第二行", "第一行\r第二行", "第一行\r\n第二行"})
    void formattingKeepsOneLineWithoutChangingTheOriginalTitle(String title) {
        Task task = new Task(1, title);
        for (TaskFormatter formatter : new TaskFormatter[]{new TextFormatter(), new ChecklistFormatter()}) {
            String formatted = formatter.format(task);
            assertFalse(formatted.contains("\n"));
            assertFalse(formatted.contains("\r"));
            assertEquals(title, task.getTitle());
        }
    }

    @Test
    void builtInFormattersRejectNullTasks() {
        assertThrows(IllegalArgumentException.class, () -> new TextFormatter().format(null));
        assertThrows(IllegalArgumentException.class, () -> new ChecklistFormatter().format(null));
    }

    private static class IdOnlyFormatter implements TaskFormatter {
        @Override
        public String format(Task task) {
            return "ID=" + task.getId();
        }
    }

    private static class RejectInvocationFormatter implements TaskFormatter {
        @Override
        public String format(Task task) {
            fail("无效输入或空数组不应该调用格式化器。");
            return "";
        }
    }
}
