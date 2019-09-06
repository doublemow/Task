package com.sumavision.launcher.task.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

/**
 * @author chen qi
 */
@Component
public class TaskScheduling {
    private List<ScheduledFuture> futureList = new CopyOnWriteArrayList<>();
    private static final int START_POSITION = 1;
    private TaskContainer exposedContainer;
    private ScheduledExecutorFactory scheduledExecutorFactory;

    @Autowired
    public TaskScheduling(TaskContainer exposedContainer,ScheduledExecutorFactory scheduledExecutorFactory){
        this.exposedContainer = exposedContainer;
        this.scheduledExecutorFactory = scheduledExecutorFactory;
    }

    /**
     * 添加任务
     * @param time 时间
     * @param method 回调方法
     * @param clazz Class
     * @param args 参数
     */
    public void addTask(Date time, Method method, Object clazz, Object[] args){
        addTaskValue(time, method, clazz, args, false);
    }

    /**
     * 添加任务
     * @param time 时间
     * @param method 回调方法
     * @param clazz Class
     * @param args 参数
     */
    public void addIfAbsent(Date time, Method method, Object clazz, Object[] args){
        addTaskValue(time, method, clazz, args, true);
    }

    /**
     *
     * @param time 时间
     * @param method 回调方法
     * @param clazz Class
     * @param args 参数
     * @param onlyIfAbsent 如果是true 则不删除原有数据
     * */
    final void addTaskValue(Date time, Method method, Object clazz, Object[] args, boolean onlyIfAbsent){
        //如果容器为空，初始化任务执行工厂
        if(exposedContainer == null){
            return;
        }

        if(exposedContainer.size() > 0 && !onlyIfAbsent){
            int index = exposedContainer.remove(time, method, clazz, args);
            if(index == START_POSITION){
                stopTask();
                if(exposedContainer.size() > 0){
                    registerHeadTask();
                }
            }

        }

        if(exposedContainer.size() == 0){
            exposedContainer.put(time,method,clazz,args);
            registerHeadTask();
        } else if(exposedContainer.compareFirstTime(time) < 0){
            exposedContainer.put(time,method,clazz,args);
            stopTask();
            registerHeadTask();
        } else{
            exposedContainer.put(time,method,clazz,args);
        }
    }

    /**
     * 注册头任务
     */
    public void registerHeadTask(){
        ScheduledExecutorTask scheduledExecutorTask = exposedContainer.getHeadTask();
        registerTask(scheduledExecutorTask);
    }

    /**
     * 注册任务
     * @param scheduledExecutorTask
     */
    public void registerTask(ScheduledExecutorTask scheduledExecutorTask){
        ScheduledExecutorService scheduledExecutorService = scheduledExecutorFactory.getObject();
        ScheduledFuture scheduledTaskFuture = scheduledExecutorFactory.registerTask(scheduledExecutorTask,scheduledExecutorService);
        ScheduledFuture taskSchedulingFuture = scheduledExecutorService.schedule(new TaskSchedulingRunnable(),scheduledExecutorTask.getDelay(),scheduledExecutorTask.getTimeUnit());
        futureList.add(scheduledTaskFuture);
        futureList.add(taskSchedulingFuture);
    }

    /**
     * 删除头任务
     */
    public void destroyHeadTask(){
        if(exposedContainer.size() > 0){
            exposedContainer.removeHead();
        }

        cleanFuture();
    }

    /**
     * 停止提交任务
     */
    public void stopTask(){
        for(ScheduledFuture future : futureList){
            future.cancel(true);
        }

        cleanFuture();
    }

    /**
     * 清空发布任务
     */
    public void cleanFuture(){
        if(futureList != null && futureList.size() > 0){
            futureList.clear();
        }
    }

    /*public void initialize(TaskContainer taskContainer,ScheduledExecutorFactory scheduledExecutorServiceFactory){
        exposedContainer = taskContainer;
        scheduledExecutorFactory = scheduledExecutorServiceFactory;
    }*/

    public TaskContainer getObject(){
        return exposedContainer;
    }
}
