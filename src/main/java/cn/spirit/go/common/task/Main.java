package cn.spirit.go.common.task;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Main {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) throws Exception {
        CronScheduler scheduler = new CronScheduler();
        long startTime = System.currentTimeMillis();

        scheduler.schedule("every5sec", "*/5 * * * * *", () -> {
            long elapsed = (System.currentTimeMillis() - startTime) / 1000;
            System.out.printf("[%s] [%s] 每5秒执行 (已运行%ds)%n",
                LocalDateTime.now().format(FMT), "every5sec", elapsed);
        });

        scheduler.schedule("everyMinute10sec", "10 * * * * *", () -> {
            System.out.printf("[%s] [%s] 每分钟第10秒执行%n",
                LocalDateTime.now().format(FMT), "everyMinute10sec");
        });

        scheduler.schedule("every2min", "*/2 * * * *", () -> {
            System.out.printf("[%s] [%s] 每2分钟执行 (5字段格式)%n",
                LocalDateTime.now().format(FMT), "every2min");
        });

        scheduler.schedule("workday9am", "0 0 9 * * 1-5", () -> {
            System.out.printf("[%s] [%s] 工作日9:00执行%n",
                LocalDateTime.now().format(FMT), "workday9am");
        });

        System.out.println("=== CronScheduler Demo ===");
        System.out.println("启动时间: " + LocalDateTime.now().format(FMT));
        System.out.println("活跃任务数: " + scheduler.getActiveTaskCount());
        System.out.println("按 Ctrl+C 停止\n");

        scheduler.start();

        Thread.sleep(30_000);

        System.out.println("\n=== 30秒后，动态添加一个新任务 ===");
        scheduler.schedule("every3sec", "*/3 * * * * *", () -> {
            System.out.printf("[%s] [%s] 每3秒执行 (动态添加)%n",
                LocalDateTime.now().format(FMT), "every3sec");
        });
        System.out.println("活跃任务数: " + scheduler.getActiveTaskCount());

        Thread.sleep(15_000);

        System.out.println("\n=== 取消 every5sec 任务 ===");
        CronTask cancelled = scheduler.getTask("every5sec");
        scheduler.cancel("every5sec");
        System.out.println("every5sec 已执行次数: " + (cancelled != null ? cancelled.getExecutionCount() : 0));
        System.out.println("活跃任务数: " + scheduler.getActiveTaskCount());

        Thread.sleep(10_000);

        System.out.println("\n=== 停止调度器 ===");
        scheduler.stop();
        System.out.println("调度器已停止，活跃任务数: " + scheduler.getActiveTaskCount());
    }
}
