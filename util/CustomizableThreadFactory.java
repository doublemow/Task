package com.sumavision.launcher.task.util;

import java.util.concurrent.ThreadFactory;

/**
 * @author chen qi
 */
public class CustomizableThreadFactory extends CustomizableThread implements ThreadFactory {

    public CustomizableThreadFactory() {
        super();
    }

    public CustomizableThreadFactory(String threadNamePrefix) {
        super(threadNamePrefix);
    }

    @Override
    public Thread newThread(Runnable runnable) {
        Thread thread = new Thread(getThreadGroup(), runnable, nextThreadName());
        thread.setPriority(getThreadPriority());
        thread.setDaemon(isDaemon());
        return thread;
    }

}
