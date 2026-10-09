package com.dailystudy.day003;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Scanner;

/** Day 003: input, validation, branches, loops, and methods. */
public class InteractiveStudyPlanner {
    public static void main(String[] args) {
        try (Scanner input = new Scanner(System.in, StandardCharsets.UTF_8)) {
            run(input, System.out);
        }
    }

    static void run(Scanner input, PrintStream out) {
        out.println("学习计划器：输入 q 可退出；小数请使用英文句点，例如 2.5。");
        while (true) {
            String name = readName(input, out);
            if (name == null) break;
            Integer days = readInt(input, out, "每周学习天数（1–7）：", 1, 7);
            if (days == null) break;
            Double hours = readHours(input, out);
            if (hours == null) break;
            Integer weeks = readInt(input, out, "计划周数（1–52）：", 1, 52);
            if (weeks == null) break;

            printReport(out, name, days, hours, weeks);
            String choice = readChoice(input, out);
            if (choice == null || choice.equalsIgnoreCase("n")) break;
        }
        out.println("已退出学习计划器。");
    }

    private static String readLine(Scanner input, PrintStream out, String prompt) {
        out.print(prompt);
        out.flush();
        if (!input.hasNextLine()) return null;
        String text = input.nextLine().strip();
        return text.equalsIgnoreCase("q") ? null : text;
    }

    private static String readName(Scanner input, PrintStream out) {
        while (true) {
            String text = readLine(input, out, "你的称呼：");
            if (text == null) return null;
            if (!text.isEmpty()) return text;
            out.println("称呼不能为空，请重新输入。");
        }
    }

    private static Integer readInt(Scanner input, PrintStream out, String prompt, int min, int max) {
        while (true) {
            String text = readLine(input, out, prompt);
            if (text == null) return null;
            try {
                int value = Integer.parseInt(text);
                if (value >= min && value <= max) return value;
            } catch (NumberFormatException ignored) {
                // Invalid input is recoverable: explain and read a new line.
            }
            out.println("请输入 " + min + "–" + max + " 之间的整数。");
        }
    }

    private static Double readHours(Scanner input, PrintStream out) {
        while (true) {
            String text = readLine(input, out, "每天学习小时（0.5–6.0）：");
            if (text == null) return null;
            try {
                double hours = Double.parseDouble(text);
                if (Double.isFinite(hours) && hours >= 0.5 && hours <= 6.0) return hours;
            } catch (NumberFormatException ignored) {
                // Number parsing and range validation are separate checks.
            }
            out.println("请输入 0.5–6.0 之间的有限数字，例如 2.5。");
        }
    }

    private static String readChoice(Scanner input, PrintStream out) {
        while (true) {
            String choice = readLine(input, out, "继续计算？（y/n）：");
            if (choice == null) return null;
            if (choice.equalsIgnoreCase("y") || choice.equalsIgnoreCase("n")) return choice;
            out.println("请输入 y 或 n，或者 q 退出。");
        }
    }

    static double calculateWeeklyHours(int days, double hours) {
        return days * hours;
    }

    static double calculateTotalHours(double weeklyHours, int weeks) {
        return weeklyHours * weeks;
    }

    static String describePace(double weeklyHours) {
        if (weeklyHours < 8.0) {
            return "低于建议投入：可缩小每周任务或延长周期。";
        } else if (weeklyHours <= 12.0) {
            return "符合当前节奏：保持练习、验证与复盘。";
        } else {
            return "高于建议投入：留出休息和复盘时间。";
        }
    }

    private static void printReport(PrintStream out, String name, int days, double hours, int weeks) {
        double weeklyHours = calculateWeeklyHours(days, hours);
        double totalHours = calculateTotalHours(weeklyHours, weeks);
        out.println("\n学习者：" + name);
        out.printf(Locale.ROOT, "每周学习小时：%.2f%n", weeklyHours);
        out.printf(Locale.ROOT, "%d 周总学习小时：%.2f%n", weeks, totalHours);
        out.println(describePace(weeklyHours));

        out.println("前 3 周累计预览（不超过实际计划周数）：");
        double accumulated = 0.0;
        for (int week = 1; week <= weeks && week <= 3; week++) {
            accumulated += weeklyHours;
            out.printf(Locale.ROOT, "第 %d 周累计：%.2f 小时%n", week, accumulated);
        }
    }
}
