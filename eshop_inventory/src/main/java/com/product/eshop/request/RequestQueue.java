package com.product.eshop.request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 请求的内存队列
 * @author yangqian
 * @date 2019/8/28
 */
public class RequestQueue {
    /**
     * 内存队列
     */
    private List<ArrayBlockingQueue<Request>> queues = new ArrayList<ArrayBlockingQueue<Request>>();
    /**
     *
     */
    private Map<Integer, Boolean> flagMap = new ConcurrentHashMap<>();

    /**
     * 添加到内存队列中
     * @param queue
     */
    public void addQueue(ArrayBlockingQueue<Request> queue) {
        this.queues.add(queue);
    }

    /**
     *
     * 使用静态内部类的方式 去初始化单例
     */
    private static class Singleton {
        private static RequestQueue instance;
        static {
            instance = new RequestQueue();
        }
        public static RequestQueue getInstance() {
            return instance;
        }
    }

    /**
     * jvm的机制去保证多线程并发安全
     * 内部类的初始化，一定只会发生一次，不管多少个线程并发去初始化
     * @return
     */
    public static RequestQueue getInstance() {
        return RequestQueue.Singleton.getInstance();
    }

    /**
     * 获取内存队列的数量
     * @return
     */
    public int queueSize() {
        return queues.size();
    }

    /**
     * 根据下标获取对应内存队列
     * @param index
     * @return
     */
    public ArrayBlockingQueue<Request> getQueue(int index) {
        return queues.get(index);
    }

    /**
     * 对应的请求是否为读或者写请求
     * @return
     */
    public Map<Integer, Boolean> getFlagMap() {
        return flagMap;
    }
}
