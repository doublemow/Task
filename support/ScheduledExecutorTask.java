package com.sumavision.launcher.task.support;

import java.util.concurrent.TimeUnit;

/**
 * @author chen qi
 */
public class ScheduledExecutorTask {
    private Runnable runnable;

    private long delay = 0;

    private long period = -1;

    private TimeUnit timeUnit = TimeUnit.MILLISECONDS;

    private boolean fixedRate = false;

    public ScheduledExecutorTask() {
    }

    public ScheduledExecutorTask(Runnable executorTask) {
        this.runnable = executorTask;
    }

    /**
     * @param executorTask
     * @param delay 延期时间 默认时间单位(ms)
     */
    public ScheduledExecutorTask(Runnable executorTask, long delay) {
        this.runnable = executorTask;
        this.delay = delay;
    }

    /**
     * @param executorTask
     * @param delay 延期时间 默认时间单位(ms)
     * @param period 重复任务中间执行的时间周期 默认时间单位(ms)
     * @param fixedRate 是否为固定周期执行
     */
    public ScheduledExecutorTask(Runnable executorTask, long delay, long period, boolean fixedRate) {
        this.runnable = executorTask;
        this.delay = delay;
        this.period = period;
        this.fixedRate = fixedRate;
    }

    public void setRunnable(Runnable executorTask) {
        this.runnable = executorTask;
    }

    public Runnable getRunnable() {
        return this.runnable;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public long getDelay() {
        return this.delay;
    }

    public void setPeriod(long period) {
        this.period = period;
    }

    public long getPeriod() {
        return this.period;
    }

    /**
     * 是否为一次性任务
     */
    public boolean isOneTimeTask() {
        return (this.period <= 0);
    }

    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = (timeUnit != null ? timeUnit : TimeUnit.MILLISECONDS);
    }

    public TimeUnit getTimeUnit() {
        return this.timeUnit;
    }

    /**
     * 设置是否为固定周期执行，默认是"false",即使用"scheduleWithFixedDelay"方法
     * <p>详细信息参照java ScheduledExecutorService
     * @see java.util.concurrent.ScheduledExecutorService#scheduleWithFixedDelay(java.lang.Runnable, long, long, java.util.concurrent.TimeUnit)
     * @see java.util.concurrent.ScheduledExecutorService#scheduleAtFixedRate(java.lang.Runnable, long, long, java.util.concurrent.TimeUnit)
     */
    public void setFixedRate(boolean fixedRate) {
        this.fixedRate = fixedRate;
    }

    public boolean isFixedRate() {
        return this.fixedRate;
    }
}
