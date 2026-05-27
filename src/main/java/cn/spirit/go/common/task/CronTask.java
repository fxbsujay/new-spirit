package cn.spirit.go.common.task;

public class CronTask implements Comparable<CronTask> {

    final String id;
    final CronExpression cronExpression;
    final Runnable task;
    volatile long nextFireTimeMs;
    volatile boolean cancelled;
    volatile int executionCount;

    public CronTask(String id, String cronExpression, Runnable task) {
        this.id = id;
        this.cronExpression = new CronExpression(cronExpression);
        this.task = task;
        this.cancelled = false;
        this.executionCount = 0;
    }

    public String getId() {
        return id;
    }

    public CronExpression getCronExpression() {
        return cronExpression;
    }

    public int getExecutionCount() {
        return executionCount;
    }

    public void cancel() {
        this.cancelled = true;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    void executed() {
        executionCount++;
    }

    @Override
    public int compareTo(CronTask other) {
        return Long.compare(this.nextFireTimeMs, other.nextFireTimeMs);
    }
}
