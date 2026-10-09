package com.dailystudy.day003;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class InteractiveStudyPlannerTest {
    private String interact(String text) {
        var bytes = new ByteArrayOutputStream();
        try (Scanner input = new Scanner(text);
             PrintStream output = new PrintStream(bytes, true, StandardCharsets.UTF_8)) {
            InteractiveStudyPlanner.run(input, output);
        }
        return bytes.toString(StandardCharsets.UTF_8);
    }

    @Test
    void baselineCalculationsMatchDay001() {
        double weekly = InteractiveStudyPlanner.calculateWeeklyHours(4, 2.5);
        assertEquals(10.0, weekly);
        assertEquals(480.0, InteractiveStudyPlanner.calculateTotalHours(weekly, 48));
    }

    @Test
    void classifiesBothBoundariesOfRecommendedPace() {
        assertTrue(InteractiveStudyPlanner.describePace(7.99).startsWith("低于"));
        assertTrue(InteractiveStudyPlanner.describePace(8.0).startsWith("符合"));
        assertTrue(InteractiveStudyPlanner.describePace(12.0).startsWith("符合"));
        assertTrue(InteractiveStudyPlanner.describePace(12.01).startsWith("高于"));
    }

    @Test
    void happyPathPrintsChineseNameAndCumulativePreview() {
        String result = interact("前端开发者\n4\n2.5\n48\nn\n");
        assertTrue(result.contains("学习者：前端开发者"));
        assertTrue(result.contains("每周学习小时：10.00"));
        assertTrue(result.contains("48 周总学习小时：480.00"));
        assertTrue(result.contains("第 3 周累计：30.00"));
        assertTrue(result.contains("已退出学习计划器。"));
    }

    @Test
    void invalidIntegerInputRetriesUntilValid() {
        String result = interact("A\nabc\n0\n8\n4\n2.5\n48\nn\n");
        assertEquals(3, result.split("请输入 1–7 之间的整数。", -1).length - 1);
        assertTrue(result.contains("480.00"));
    }

    @Test
    void rejectsNonFiniteAndOutOfRangeHoursThenRecovers() {
        String result = interact("A\n4\nabc\nNaN\nInfinity\n1e1000\n0\n6.1\n2.5\n48\nn\n");
        assertEquals(6, result.split("请输入 0.5–6.0 之间的有限数字", -1).length - 1);
        assertTrue(result.contains("480.00"));
    }

    @Test
    void weeksRequireAnIntegerWithinRange() {
        String result = interact("A\n4\n2.5\n2.5\n0\n53\n48\nn\n");
        assertEquals(3, result.split("请输入 1–52 之间的整数。", -1).length - 1);
        assertTrue(result.contains("480.00"));
    }

    @Test
    void yStartsAnotherCalculationWithoutReusingPreviousInputs() {
        String result = interact("A\n4\n2.5\n48\nY\nB\n4\n2\n10\nn\n");
        assertTrue(result.contains("学习者：A"));
        assertTrue(result.contains("学习者：B"));
        assertTrue(result.contains("48 周总学习小时：480.00"));
        assertTrue(result.contains("10 周总学习小时：80.00"));
        assertEquals(1, result.split("已退出学习计划器。", -1).length - 1);
    }

    @Test
    void qAtEachPromptExitsWithoutPrintingPartialReport() {
        for (String text : new String[]{"q\n", "A\nQ\n", "A\n4\nq\n", "A\n4\n2.5\nq\n"}) {
            String result = interact(text);
            assertTrue(result.contains("已退出"));
            assertFalse(result.contains("总学习小时"));
        }
    }

    @Test
    void endOfInputAtEachStageIsGraceful() {
        for (String text : new String[]{"", "A\n", "A\n4\n", "A\n4\n2.5\n"}) {
            String result = interact(text);
            assertTrue(result.contains("已退出"));
            assertFalse(result.contains("总学习小时"));
        }
        assertTrue(interact("A\n4\n2.5\n48\n").contains("480.00"));
    }

    @Test
    void invalidContinueChoiceRetriesInsteadOfStartingAnotherReport() {
        String result = interact("A\n4\n2.5\n48\nmaybe\n\nn\n");
        assertEquals(2, result.split("请输入 y 或 n", -1).length - 1);
        assertEquals(1, result.split("学习者：A", -1).length - 1);
    }

    @Test
    void blankNamesRetryAndInputWhitespaceIsTrimmed() {
        String result = interact("\n   \n  A  \n 4 \n 2.5 \n 48 \n n \n");
        assertEquals(2, result.split("称呼不能为空", -1).length - 1);
        assertTrue(result.contains("学习者：A\n"));
        assertTrue(result.contains("480.00"));
    }

    @Test
    void shortPlansDoNotPreviewNonexistentWeeks() {
        String result = interact("A\n4\n2.5\n2\nn\n");
        assertTrue(result.contains("第 2 周累计：20.00"));
        assertFalse(result.contains("第 3 周累计"));
    }
}
