package com.product.eshop.listener;

import com.product.eshop.kafka.KafkaConsumer;
import com.product.eshop.queue.RebuildCacheQueue;
import com.product.eshop.queue.RebuildCacheThread;
import com.product.eshop.spring.SpringContext;
import com.product.eshop.threadpool.RequestProcessorThreadPool;
import com.product.eshop.zookeeper.ZooKeeperSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * 系统初始化监听器
 * @author yangqian
 * @date 2019/8/28
 */
@Slf4j
public class InitListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        log.info("系统初始化");
        log.info("初始化applicationContext");

        // 用于设置ApplicationContext
        ServletContext sc = sce.getServletContext();
        ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(sc);
        SpringContext.setApplicationContext(context);

        // 初始化zookeeper会话连接
        ZooKeeperSession.init();
        // 初始化工作线程池和工作队列
        RequestProcessorThreadPool.init();
        RebuildCacheQueue.init();
        // 启动kafka的消费线程
        new Thread(new KafkaConsumer("cache-message")).start();
        // 启动队列线程
        new Thread(new RebuildCacheThread()).start();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }
}
