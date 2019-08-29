package com.product.eshop.threadpool.thread;

import com.product.eshop.request.Request;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;

/**
 * 执行请求的工作线程
 * @author yangqian
 * @date 2019/8/28
 */
@Slf4j
public class RequestProcessorThread implements Callable<Boolean> {
    /**
     * 自己监控的内存队列
     */
    private ArrayBlockingQueue<Request> queue;

    public RequestProcessorThread(ArrayBlockingQueue<Request> queue) {
        this.queue = queue;
    }

    @Override
    public Boolean call() {
        try {
            while (true) {
                // ArrayBlockingQueue
                // Blocking就是说明，如果队列满了，或者是空的，那么都会在执行操作的时候阻塞住，等待有数据后执行
                Request request = queue.take();
                request.process();
            }
        } catch (Exception e) {
            log.error("error execute thread process ... ... ", e);
        }
        return true;
    }
}
