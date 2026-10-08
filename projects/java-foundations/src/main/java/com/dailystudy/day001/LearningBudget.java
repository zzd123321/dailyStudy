package com.dailystudy.day001;

/** Day 001: variables, arithmetic, and output. */
public class LearningBudget {
    public static void main(String[] args) {
        String learner = "AI 全栈学习者";
        int studyDaysPerWeek = 4;
        double studyHoursPerDay = 2.5;
        int totalWeeks = 48;

        double weeklyHours = studyDaysPerWeek * studyHoursPerDay;
        double plannedHours = weeklyHours * totalWeeks;

        System.out.println("学习者：" + learner);
        System.out.println("每周学习天数：" + studyDaysPerWeek);
        System.out.println("每天学习小时：" + studyHoursPerDay);
        System.out.println("每周学习小时：" + weeklyHours);
        System.out.println(totalWeeks + "周计划学习小时：" + plannedHours);
    }
}
