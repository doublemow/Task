package com.sumavision.launcher.task.util;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.concurrent.*;

/**
 * @author chen qi
 */
public abstract class AbstractCustomizableExecutor extends CustomizableThreadFactory implements InitializingBean, DisposableBean {
    private ThreadFactory threadFactory = this;

    private boolean threadNamePrefixSet = false;

    private RejectedExecutionHandler rejectedExecutionHandler = new ThreadPoolExecutor.AbortPolicy();

    private boolean waitForTasksToCompleteOnShutdown = false;

    private int awaitTerminationSeconds = 0;

    private String threadName;

    private ExecutorService executor;


    public void setThreadFactory(ThreadFactory threadFactory) {
        this.threadFactory = (threadFactory != null ? threadFactory : this);
    }

    @Override
    public void setThreadNamePrefix(String threadNamePrefix) {
        super.setThreadNamePrefix(threadNamePrefix);
        this.threadNamePrefixSet = true;
    }

    /**
     * 设置终止策略
     */
    public void setRejectedExecutionHandler(RejectedExecutionHandler rejectedExecutionHandler) {
        this.rejectedExecutionHandler =
                (rejectedExecutionHandler != null ? rejectedExecutionHandler : new ThreadPoolExecutor.AbortPolicy());
    }

    /**
     * 停止线程池时是否等待已有任务
     */
    public void setWaitForTasksToCompleteOnShutdown(boolean waitForJobsToCompleteOnShutdown) {
        this.waitForTasksToCompleteOnShutdown = waitForJobsToCompleteOnShutdown;
    }

    /**
     * 设置等待线程执行时间
     */
    public void setAwaitTerminationSeconds(int awaitTerminationSeconds) {
        this.awaitTerminationSeconds = awaitTerminationSeconds;
    }

    public void setThreadName(String name) {
        this.threadName = name;
    }

    /**
     * 设置 ExecutorService
     */
    public void initialize() {
        if (!this.threadNamePrefixSet && this.threadName != null) {
            setThreadNamePrefix(this.threadName + "-");
        }
        this.executor = initializeExecutor(this.threadFactory, this.rejectedExecutionHandler);
    }

    /**
     * 创建实例
     * @param threadFactory 线程工厂
     * @param rejectedExecutionHandler 拒绝策略
     * @return ExecutorService
     */
    protected abstract ExecutorService initializeExecutor(
            ThreadFactory threadFactory, RejectedExecutionHandler rejectedExecutionHandler);

    private void shutdown() {
        if (this.waitForTasksToCompleteOnShutdown) {
            this.executor.shutdown();
        }
        else {
            this.executor.shutdownNow();
        }
        awaitTerminationIfNecessary();
    }

    /**
     * 等待一定时间后关闭线程池
     */
    private void awaitTerminationIfNecessary() {
        if (this.awaitTerminationSeconds > 0) {
            try {
                this.executor.awaitTermination(this.awaitTerminationSeconds, TimeUnit.SECONDS);
            }
            catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void destroy() {
        shutdown();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initialize();
    }
}
