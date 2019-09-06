package com.sumavision.launcher.task.support;

import com.sumavision.launcher.listener.ScheduledExecutorTaskListener;

/**
 * @author cq
 */
public class TaskSchedulingRunnable implements Runnable{
    private TaskScheduling taskScheduling = ScheduledExecutorTaskListener.getTaskScheduling();
    private TaskContainer taskContainer = taskScheduling.getObject();

    @Override
    public void run() {
        if(taskContainer == null){
            return;
        }

        if(taskContainer.size() > 0){
            taskScheduling.destroyHeadTask();
        }

        if(taskContainer.size() > 0){
            ScheduledExecutorTask scheduledExecutorTask = taskContainer.getHeadTask();
            taskScheduling.registerTask(scheduledExecutorTask);
        }
    }

}
