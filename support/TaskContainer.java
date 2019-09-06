package com.sumavision.launcher.task.support;

import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <p>功能描述：用于存储任务的容器<p/>
 * @author chen qi
 */
@Component
public class TaskContainer {
    /**容器最大容量*/
    private static final int DEFAULT_INITIAL_CAPACITY = 11;
    /**容器默认大小*/
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
    private static final int START_POSITION = 1;
    private volatile Node[] queue ;
    private volatile int size;
    /** 使用所有公共操作*/
    private final ReentrantLock lock;
    private AtomicInteger allocationSpinLock = new AtomicInteger(0);

    /**
     * 初始化一个指定大小的容器
     * @param initialCapacity
     */
    public TaskContainer(int initialCapacity) {
        if (initialCapacity < 1){
            throw new IllegalArgumentException();
        }
        this.lock = new ReentrantLock();
        this.queue = new Node[initialCapacity];
    }

    /**
     *  初始化一个空容器，默认大小为11
     */
    public TaskContainer(){
        this(DEFAULT_INITIAL_CAPACITY);
    }

    /**
     * 插入
     * @param time 时间
     * @param method 回调方法
     * @param clazz Class
     * @param args 参数
     * @return boolean
     */
    public boolean put(Date time, Method method, Object clazz, Object[] args){
        if (time == null || method == null || clazz == null){ throw new NullPointerException();}
        lock.lock();
        int n, cap;
        Node[] array;
        while ((n = size) >= ((cap = (array = queue).length) - 1)){
            tryGrow(array, cap);
        }
        Node f = new Node(time,method,clazz,args);
        try {
            ++n;
            array[n] = f;
            circleSwap(n,array);

            size = n;
        } finally {
            lock.unlock();
        }
        return true;
    }

    /**
     * 删除头结点
     * @return boolean
     */
    public boolean removeHead(){
        final ReentrantLock lock = this.lock;
        lock.lock();
        int n;
        try {
            if((n = size) == 0){
                return false;
            }

            queue[START_POSITION] = queue[n];
            queue[n] = null;
            --n;
            size = n;

            if(n == START_POSITION){
                return true;
            }

            headify(START_POSITION);

            return true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 删除节点
     * @param time 时间
     * @param method 方法
     * @param clazz Class
     * @param args 参数
     * @return int
     */
    public int remove(Date time, Method method, Object clazz, Object[] args){
        final ReentrantLock lock = this.lock;
        lock.lock();
        int n;
        try{
            if((n = size) == 0){
                return -1;
            }

            int index = index(time, method, clazz, args);
            if(index < 0){
                return -1;
            }

            rangeCheck(index);

            queue[index] = queue[n];
            queue[n] = null;
            --n;
            size = n;

            if(n == START_POSITION){
                return index;
            }

            headify(index);

            return index;
        }finally {
            lock.unlock();
        }
    }

    /**
     * 从位置p开始与子节点进行比较交换位置
     * @param p 比较起始位置
     */
    private void headify(int p){
        Node[] array = queue;
        int left, right;
        int n = array.length;
        Node l, r, node;
        while(size >= p << 1){
            left = p << 1;
            right = (p << 1) + 1;

            l = array[left];
            if(n >= right){
                r = array[right];
                if(r != null && l.getTime().compareTo(r.getTime()) > 0){
                    left = right;
                    l = r;
                }
            }

            node = array[p];
            if(l.getTime().compareTo(node.getTime()) < 0){
                swap(array,p,left);
                p = left;
            }else{
                break;
            }
        }
    }

    /**
     * 查询容器使用数量
     * @return int
     */
    public int size() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return size;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 与第一个任务比较时间，
     * @param time 比较时间
     * @return 如果大于头任务时间返回1，等于返回0，小于返回-1
     */
    public int compareFirstTime(Date time){
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            rangeCheck(START_POSITION);
            Node n = queue[START_POSITION];
            int result = time.compareTo(n.getTime());
            return result;
        }finally {
            lock.unlock();
        }
    }

    public ScheduledExecutorTask getHeadTask(){
        final ReentrantLock lock = this.lock;
        lock.lock();
        Node node;
        try {
            if(size == 0){
                throw new NullPointerException();
            }
            node = queue[START_POSITION];
            return createTask(node);
        }finally {
            lock.unlock();
        }
    }

    private ScheduledExecutorTask createTask(Node node){
        Node n = node;
        Long delayTime = ((delayTime = n.getTime().getTime() - System.currentTimeMillis()) > 0L) ? delayTime : 0L;
        ScheduledMethodRunnable scheduledMethodRunnable = new ScheduledMethodRunnable(n.getMethod(),n.getClazz(),n.getArgs());
        n.setRunnable(scheduledMethodRunnable);
        n.setDelay(delayTime);
        return n;
    }

    private void rangeCheck(int index) {
        if (index > size){
            throw new IndexOutOfBoundsException();
        }
    }

    /**
     * 查询对象位置，如果没有找到返回-1
     * @param time 时间
     * @param method 方法
     * @param clazz Class
     * @param args 参数
     * @return int
     */
    private int index(Date time, Method method, Object clazz, Object[] args){
        int n = size;
        Node[] tab = queue;
        if(n <= 0){
            return -1;
        }

        Node node = new Node(time,method,clazz,args);
        for(int i = START_POSITION; i < n; ++i){
            if(node.equals(tab[i])){
                return i;
            }
        }

        return -1;
    }

    /**
     * 循环比较大小
     * @param p 当前位置
     * @param array 循环数组
     */
    private void circleSwap(int p,Node[] array){
        if((p>>>1) < START_POSITION){
            return;
        }

        Node n = array[p];
        Node parent = array[p>>>1];

        if(n.getTime().compareTo(parent.getTime()) < 0){
            swap(array,p>>>1, p);
            circleSwap(p >>>1, array);
        }
    }

    /**
     * 交换位置
     * @param a 交换对象
     * @param p 交换节点位置
     * @param q 交换节点位置
     */
    private void swap(Node[] a, int p, int q){
        Node t = a[p];
        a[p] = a[q];
        a[q] = t;
    }

    /**
     * 尝试增长数组以容纳至少一个元素(但通常会扩大约50%)
     * @param array
     * @param oldCap
     */
    private void tryGrow(Node[] array, int oldCap) {
        lock.unlock();
        Node[] newArray = null;
        if (allocationSpinLock.compareAndSet(0,1)) {
            try {
                int newCap = oldCap + ((oldCap < 64) ?
                        (oldCap + 2) :
                        (oldCap >> 1));
                if (newCap - MAX_ARRAY_SIZE > 0) {
                    int minCap = oldCap + 1;
                    if (minCap < 0 || minCap > MAX_ARRAY_SIZE){
                        throw new OutOfMemoryError();
                    }
                    newCap = MAX_ARRAY_SIZE;
                }
                if (newCap > oldCap && queue == array){
                    newArray = new Node[newCap];
                }
            } finally {
                allocationSpinLock.set(0);
            }
        }
        if (newArray == null){
            Thread.yield();
        }
        lock.lock();
        if (newArray != null && queue == array) {
            queue = newArray;
            System.arraycopy(array, 0, newArray, 0, oldCap);
        }
    }

    static class Node extends ScheduledExecutorTask{
        final Date time;
        final Method method;
        final Object clazz;
        final Object[] args;

        Node(Date time,Method method,Object clazz,Object[] args){
            this.time = time;
            this.method = method;
            this.clazz = clazz;
            this.args = args;
        }

        public final Date getTime() {
            return time;
        }

        public final Method getMethod() {
            return method;
        }

        public final Object getClazz() {
            return clazz;
        }

        public final Object[] getArgs() {
            return args;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Node node = (Node) o;

            if (!method.equals(node.method)) {
                return false;
            }

            return Arrays.equals(args, node.args);
        }

        @Override
        public int hashCode() {
            int result = method.hashCode();
            result = 31 * result + Arrays.hashCode(args);
            return result;
        }
    }

}
