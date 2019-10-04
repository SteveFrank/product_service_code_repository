package com.product.eshop.queue;

import com.product.eshop.model.ProductInfo;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * 重建缓存的内存队列
 * @author yangqian
 * @date 2019/10/3
 */
public class RebuildCacheQueue {

    private ArrayBlockingQueue<ProductInfo> queue = new ArrayBlockingQueue<ProductInfo>(1000);

    /**
     * 将需要处理的商品信息加入到队列中
     * @param productInfo 产品信息
     */
    public void putProductInfo(ProductInfo productInfo) {
        try {
            queue.put(productInfo);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public ProductInfo takeProductInfo() {
        try {
            return queue.take();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static class Singleton {
        private static RebuildCacheQueue instance;
        static {
            instance = new RebuildCacheQueue();
        }
        public static RebuildCacheQueue getInstance() {
            return instance;
        }
    }

    public static RebuildCacheQueue getInstance() {
        return Singleton.getInstance();
    }

    public static void init() {
        getInstance();
    }

}
