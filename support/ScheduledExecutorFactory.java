package com.sumavision.launcher.task.support;

import com.sumavision.launcher.task.util.AbstractCustomizableExecutor;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.util.ObjectUtils;

import java.util.concurrent.*;

/**
 * <p>描述：用于定时发布信息位、组件自定义<p/>
 * @author chen qi
 */
public class ScheduledExecutorFactory extends AbstractCustomizableExecutor implements FactoryBean<ScheduledExecutorService> {

    private int poolSize = 1;

    private ScheduledExecutorTask[] scheduledExecutorTasks;

    private ScheduledExecutorService exposedExecutor;

    private boolean exposeUnConfigurableExecutor = false;

    public void setPoolSize(int poolSize) {
        if(poolSize > 0){
            this.poolSize = poolSize;
        }
    }

    public void setScheduledExecutorTasks(ScheduledExecutorTask... scheduledExecutorTasks) {
        this.scheduledExecutorTasks = scheduledExecutorTasks;
    }

    @Override
    protected ExecutorService initializeExecutor(
            ThreadFactory threadFactory, RejectedExecutionHandler rejectedExecutionHandler) {

        ScheduledExecutorService executor =
                createExecutor(this.poolSize, threadFactory, rejectedExecutionHandler);

        if (!ObjectUtils.isEmpty(this.scheduledExecutorTasks)) {
            registerTasks(this.scheduledExecutorTasks, executor);
        }

        //使用不可变的装饰器装饰
        this.exposedExecutor = (this.exposeUnConfigurableExecutor ?
                Executors.unconfigurableScheduledExecutorService(executor) : executor);

        return executor;
    }

    private ScheduledExecutorService createExecutor(
            int poolSize, ThreadFactory threadFactory, RejectedExecutionHandler rejectedExecutionHandler) {

        return new ScheduledThreadPoolExecutor(poolSize, threadFactory, rejectedExecutionHandler);
    }

    public ScheduledFuture registerTask(ScheduledExecutorTask task, ScheduledExecutorService executor){
        if (task.isOneTimeTask()) {
            return executor.schedule(task.getRunnable(), task.getDelay(), task.getTimeUnit());
        }
        else {
            if (task.isFixedRate()) {
                return executor.scheduleAtFixedRate(task.getRunnable(), task.getDelay(), task.getPeriod(), task.getTimeUnit());
            }
            else {
                return executor.scheduleWithFixedDelay(task.getRunnable(), task.getDelay(), task.getPeriod(), task.getTimeUnit());
            }
        }
    }

    private void registerTasks(ScheduledExecutorTask[] tasks, ScheduledExecutorService executor) {
        for (ScheduledExecutorTask task : tasks) {
            if (task.isOneTimeTask()) {
                executor.schedule(task.getRunnable(), task.getDelay(), task.getTimeUnit());
            }
            else {
                if (task.isFixedRate()) {
                    executor.scheduleAtFixedRate(task.getRunnable(), task.getDelay(), task.getPeriod(), task.getTimeUnit());
                }
                else {
                    executor.scheduleWithFixedDelay(task.getRunnable(), task.getDelay(), task.getPeriod(), task.getTimeUnit());
                }
            }
        }
    }

    /**
     * 是否制定一个不可变的装饰器
     */
    public void setExposeUnconfigurableExecutor(boolean exposeUnConfigurableExecutor) {
        this.exposeUnConfigurableExecutor = exposeUnConfigurableExecutor;
    }

    @Override
    public ScheduledExecutorService getObject() {
        return this.exposedExecutor;
    }

    @Override
    public Class<? extends ScheduledExecutorService> getObjectType() {
        return (this.exposedExecutor != null ? this.exposedExecutor.getClass() : ScheduledExecutorService.class);
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
