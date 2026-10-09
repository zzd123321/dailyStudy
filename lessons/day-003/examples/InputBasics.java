import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/** Minimal teaching example: use valid numeric input, then study validation in the full planner. */
public class InputBasics {
    public static void main(String[] args) {
        try (Scanner input = new Scanner(System.in, StandardCharsets.UTF_8)) {
            System.out.print("你的称呼：");
            String name = input.nextLine();
            System.out.print("每周学习天数：");
            int days = Integer.parseInt(input.nextLine());
            System.out.print("每天学习小时：");
            double hours = Double.parseDouble(input.nextLine());

            double weeklyHours = calculateWeeklyHours(days, hours);
            System.out.println(name + "每周计划学习 " + weeklyHours + " 小时。");
            if (weeklyHours >= 8.0 && weeklyHours <= 12.0) {
                System.out.println("符合当前学习节奏。");
            } else {
                System.out.println("需要调整任务量或学习时间。");
            }

            for (int week = 1; week <= 3; week++) {
                System.out.println("第 " + week + " 周累计：" + (week * weeklyHours));
            }
        }
    }

    static double calculateWeeklyHours(int days, double hours) {
        return days * hours;
    }
}
