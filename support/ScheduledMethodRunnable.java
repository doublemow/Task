package com.sumavision.launcher.task.support;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;


/**
 * @author cq
 */
public class ScheduledMethodRunnable implements Runnable{
    private Object[] args;
    private Method method;
    private Object clazz;
    private Long time;

    public ScheduledMethodRunnable(Method method, Object clazz, Object[] args) {
        this.method = method;
        this.clazz = clazz;
        this.args = args;
    }

    public ScheduledMethodRunnable(Method method, Object clazz, Object[] args, Long time) {
        this.time = time;
        this.method = method;
        this.clazz = clazz;
        this.args = args;
    }
    @Override
    public void run() {
        try {
            method.invoke(clazz,args);
        } catch (InvocationTargetException ex) {
            rethrowRuntimeException(ex.getTargetException());
        }
        catch (IllegalAccessException ex) {
            throw new UndeclaredThrowableException(ex);
        }
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Object getClazz() {
        return clazz;
    }

    public void setClazz(Object clazz) {
        this.clazz = clazz;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    private void rethrowRuntimeException(Throwable ex) {
        if (ex instanceof RuntimeException) {
            throw (RuntimeException) ex;
        }
        if (ex instanceof Error) {
            throw (Error) ex;
        }
        throw new UndeclaredThrowableException(ex);
    }
}
