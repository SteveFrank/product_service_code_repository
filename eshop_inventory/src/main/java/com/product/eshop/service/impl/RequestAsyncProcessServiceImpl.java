package com.product.eshop.service.impl;

import com.product.eshop.request.Request;
import com.product.eshop.request.RequestQueue;
import com.product.eshop.request.impl.ProductInventoryDBUpdateRequest;
import com.product.eshop.service.RequestAsyncProcessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * 请求异步处理的service实现
 * @author yangqian
 * @date 2019/8/28
 */
@Slf4j
@Service("requestAsyncProcessService")
public class RequestAsyncProcessServiceImpl implements RequestAsyncProcessService {

    @Override
    public void process(Request request) {
        try {
            // 首先进行读请求的去重
            RequestQueue requestQueue = RequestQueue.getInstance();
            Map<Integer, Boolean> flagMap = requestQueue.getFlagMap();
            // 进行请求判断 然后将对应的读写请求放入map中
            if (request instanceof ProductInventoryDBUpdateRequest) {
                // 如果是一个更新数据库的请求
                // 那么就将product对应的标识设置为true
                flagMap.put(request.getProductId(), true);
            } else if (request instanceof ProductInventoryServiceImpl) {
                // 如果是读请求，首先看是否存在当前商品在map中
                Boolean flag = flagMap.get(request.getProductId());
                // 如果flag是null
                if(Objects.isNull(flag)) {
                    // 如果不存在放入
                    flagMap.put(request.getProductId(), false);
                }
                if(flag != null && flag) {
                    // 如果是缓存刷新的请求，那么就判断，如果标识不为空，而且是true，就说明之前有一个这个商品的数据库更新请求
                    // 如果存在数据更新的请求可以直接读取缓存中的数据
                    flagMap.put(request.getProductId(), false);
                }
                // 如果是缓存刷新的请求，而且发现标识不为空，但是标识是false
                // 说明前面已经有一个数据库更新请求+一个缓存刷新请求了，大家想一想
                if(flag != null && !flag) {
                    // 对于这种读请求，直接就过滤掉，不要放到后面的内存队列里面去了
                    return;
                }
            }
            // 做请求的路由 根据每个请求的商品ID 路由到对应的内存队列中
            ArrayBlockingQueue<Request> queue = getRoutingQueue(request.getProductId());
            // 将请求放入到对应的队列中 完成路由操作
            queue.put(request);
        } catch (Exception e) {
            log.error("put to queue error ... ", e);
        }
    }

    /**
     * 获取路由到对应的内存队列
     * @param productId
     * @return
     */
    private ArrayBlockingQueue<Request> getRoutingQueue(Integer productId) {
        RequestQueue requestQueue = RequestQueue.getInstance();
        // 首先将productID转成Hash值 用于路由到对应的内存队列中去
        String key = String.valueOf(productId);
        int h;
        int hash  = (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
        // 对hash值取模，将hash值路由到指定的内存队列中，比如内存队列大小8
        // 用内存队列的数量对hash值取模之后，结果一定是在0~7之间
        // 所以任何一个商品id都会被固定路由到同样的一个内存队列中去的
        int index = (requestQueue.queueSize() - 1) & hash;
        log.info("productId: {}, index:{}", productId, index);
        return requestQueue.getQueue(index);
    }

}
