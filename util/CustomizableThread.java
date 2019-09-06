package com.sumavision.launcher.task.util;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 可修饰线程
 * @author chen qi
 */
public class CustomizableThread implements Serializable {
    private String threadNamePrefix;

    private int threadPriority = Thread.NORM_PRIORITY;

    private boolean daemon = false;

    private ThreadGroup threadGroup;

    private final AtomicInteger threadCount = new AtomicInteger(0);


    public CustomizableThread() {
        this.threadNamePrefix = getDefaultThreadNamePrefix();
    }

    public CustomizableThread(String threadNamePrefix) {
        this.threadNamePrefix = (threadNamePrefix != null ? threadNamePrefix : getDefaultThreadNamePrefix());
    }


    /**
     * 设置线程名称前缀
     * 默认为"ScheduledTaskExecutor-"
     */
    public void setThreadNamePrefix(String threadNamePrefix) {
        this.threadNamePrefix = (threadNamePrefix != null ? threadNamePrefix : getDefaultThreadNamePrefix());
    }

    /**
     * 返回名称前缀
     */
    public String getThreadNamePrefix() {
        return this.threadNamePrefix;
    }

    /**
     * 设置线程优先级
     */
    public void setThreadPriority(int threadPriority) {
        this.threadPriority = threadPriority;
    }

    /**
     * 返回线程优先级
     */
    public int getThreadPriority() {
        return this.threadPriority;
    }

    /**
     * 设置守护线程
     */
    public void setDaemon(boolean daemon) {
        this.daemon = daemon;
    }

    /**
     * 判断是否是守护线程
     */
    public boolean isDaemon() {
        return this.daemon;
    }

    public void setThreadGroupName(String name) {
        this.threadGroup = new ThreadGroup(name);
    }

    public void setThreadGroup(ThreadGroup threadGroup) {
        this.threadGroup = threadGroup;
    }

    public ThreadGroup getThreadGroup() {
        return this.threadGroup;
    }

    /**
     * 返回新线程名称
     */
    protected String nextThreadName() {
        return getThreadNamePrefix() + this.threadCount.incrementAndGet();
    }

    /**
     * 为线程设置默认名称
     */
    protected String getDefaultThreadNamePrefix() {
        return "ScheduledTaskExecutor-";
    }

}
