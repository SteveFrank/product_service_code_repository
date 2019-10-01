package com.product.eshop.threadpool;

import com.product.eshop.request.Request;
import com.product.eshop.request.RequestQueue;
import com.product.eshop.threadpool.thread.RequestProcessorThread;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

/**
 * 请求处理线程池线程池 : 保证使用的时候是 单例
 * @author yangqian
 * @date 2019/8/28
 */
@Slf4j
public class RequestProcessorThreadPool {

    /**
     * 在实际的项目中
     * * 设置的线程池的大小
     * * 线程监控的内存队列的大小
     * * 这样的一些数据都应该尽量设置在外部的配置文件中
     * 此处为了简化，直接hardCode
     */
    private ExecutorService threadPool = new ThreadPoolExecutor(
            10, 10, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(), new ThreadPoolExecutor.AbortPolicy());

    /**
     * 构造方法进行内存队列的初始化
     */
    private RequestProcessorThreadPool() {
        // 存储十个队列用于Hash处理
        RequestQueue requestQueue = RequestQueue.getInstance();
        for (int i = 0; i < 10; i++) {
            // 初始化定长的BlockingQueue
            ArrayBlockingQueue<Request> arrayBlockingQueue = new ArrayBlockingQueue<Request>(100);
            // 保存到集合中
            requestQueue.addQueue(arrayBlockingQueue);
            log.info("初始化线程队列");
            // 提交到线程池执行对应的任务
            threadPool.submit(new RequestProcessorThread(arrayBlockingQueue));
        }
    }

    /**
     *
     * 使用静态内部类的方式 去初始化单例
     */
    private static class Singleton {
        private static RequestProcessorThreadPool instance;
        static {
            instance = new RequestProcessorThreadPool();
        }
        public static RequestProcessorThreadPool getInstance() {
            return instance;
        }
    }

    /**
     * jvm的机制去保证多线程并发安全
     * 内部类的初始化，一定只会发生一次，不管多少个线程并发去初始化
     * @return
     */
    public static RequestProcessorThreadPool getInstance() {
        return Singleton.getInstance();
    }

    /**
     * 初始化的便捷方法
     */
    public static void init() {
        // 初始化
        getInstance();
    }

}
