package cn.spirit.go.common.task;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class CronScheduler {

    private final PriorityQueue<CronTask> taskQueue;
    private final Map<String, CronTask> taskMap;
    private final ReentrantLock lock;
    private final Condition taskAvailable;
    private final AtomicBoolean running;
    private final ZoneId zoneId;
    private Thread schedulerThread;

    public CronScheduler() {
        this(ZoneId.systemDefault());
    }

    public CronScheduler(ZoneId zoneId) {
        this.taskQueue = new PriorityQueue<>();
        this.taskMap = new ConcurrentHashMap<>();
        this.lock = new ReentrantLock();
        this.taskAvailable = lock.newCondition();
        this.running = new AtomicBoolean(false);
        this.zoneId = zoneId;
    }

    public CronTask schedule(String id, String cronExpression, Runnable task) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Task id must not be null or empty");
        }
        if (taskMap.containsKey(id)) {
            throw new IllegalArgumentException("Task with id '" + id + "' already exists");
        }

        CronTask cronTask = new CronTask(id, cronExpression, task);
        LocalDateTime now = LocalDateTime.now(zoneId);
        LocalDateTime nextFire = cronTask.cronExpression.nextFireTime(now);
        cronTask.nextFireTimeMs = toEpochMilli(nextFire);

        lock.lock();
        try {
            taskMap.put(id, cronTask);
            taskQueue.offer(cronTask);
            taskAvailable.signal();
        } finally {
            lock.unlock();
        }

        return cronTask;
    }

    public boolean cancel(String id) {
        CronTask task = taskMap.remove(id);
        if (task != null) {
            task.cancel();
            lock.lock();
            try {
                taskQueue.remove(task);
                taskAvailable.signal();
            } finally {
                lock.unlock();
            }
            return true;
        }
        return false;
    }

    public CronTask getTask(String id) {
        return taskMap.get(id);
    }

    public int getActiveTaskCount() {
        return taskMap.size();
    }

    public void start() {
        if (!running.compareAndSet(false, true)) {
            return;
        }
        schedulerThread = new Thread(this::schedulerLoop, "cron-scheduler");
        schedulerThread.setDaemon(true);
        schedulerThread.setPriority(Thread.NORM_PRIORITY - 1);
        schedulerThread.start();
    }

    public void stop() {
        if (!running.compareAndSet(true, false)) {
            return;
        }
        lock.lock();
        try {
            taskAvailable.signal();
        } finally {
            lock.unlock();
        }
        try {
            schedulerThread.join(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public boolean isRunning() {
        return running.get();
    }

    private void schedulerLoop() {
        while (running.get()) {
            lock.lock();
            try {
                CronTask nextTask = taskQueue.peek();

                if (nextTask == null) {
                    taskAvailable.await();
                    continue;
                }

                long now = System.currentTimeMillis();
                long delay = nextTask.nextFireTimeMs - now;

                if (delay > 0) {
                    taskAvailable.awaitNanos(TimeUnit.MILLISECONDS.toNanos(delay));
                    continue;
                }

                taskQueue.poll();
                if (nextTask.cancelled) {
                    taskMap.remove(nextTask.id);
                    continue;
                }

                executeTask(nextTask);

                if (!nextTask.cancelled && taskMap.containsKey(nextTask.id)) {
                    reschedule(nextTask);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("[CronScheduler] Scheduler error: " + e.getMessage());
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }
    }

    private void executeTask(CronTask task) {
        try {
            task.task.run();
            task.executed();
        } catch (Exception e) {
            System.err.println("[CronScheduler] Task '" + task.id + "' failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void reschedule(CronTask task) {
        try {
            LocalDateTime now = LocalDateTime.now(zoneId);
            LocalDateTime nextFire = task.cronExpression.nextFireTime(now);
            task.nextFireTimeMs = toEpochMilli(nextFire);
            taskQueue.offer(task);
        } catch (Exception e) {
            taskMap.remove(task.id);
        }
    }

    private long toEpochMilli(LocalDateTime time) {
        return time.atZone(zoneId).toInstant().toEpochMilli();
    }
}
