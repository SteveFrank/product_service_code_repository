package com.product.eshop.kafka.processor;

import com.alibaba.fastjson.JSONObject;
import com.product.eshop.model.ProductInfo;
import com.product.eshop.model.ShopInfo;
import com.product.eshop.service.CacheService;
import com.product.eshop.spring.SpringContext;
import com.product.eshop.zookeeper.ZooKeeperSession;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author yangqian
 * @date 2019/10/1
 */
@Slf4j
public class KafkaMessageProcessor implements Runnable {

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private KafkaStream kafkaStream;
    private CacheService cacheService;

    public KafkaMessageProcessor(KafkaStream kafkaStream) {
        this.kafkaStream = kafkaStream;
        this.cacheService = (CacheService) SpringContext.getApplicationContext()
                .getBean("cacheService");
    }

    @Override
    public void run() {
        /**
         * 通过kafkaStream 拿到迭代器获取到消息进行处理
         */
        ConsumerIterator<byte[], byte[]> iterator = kafkaStream.iterator();
        while (iterator.hasNext()) {
            String message = new String(iterator.next().message());
            // 首先将消息转化为json对象
            JSONObject messageObject = JSONObject.parseObject(message);
            // 从这里提取出消息对应的服务标识
            String serviceId = messageObject.getString("serviceId");
            // 如果是商品信息服务
            // 如果是商品信息服务
            if("productInfoService".equals(serviceId)) {
                processProductInfoChangeMessage(messageObject);
            } else if("shopInfoService".equals(serviceId)) {
                processShopInfoChangeMessage(messageObject);
            }
        }
    }

    /**
     * 处理商品信息变更的消息
     * @param messageJSONObject
     */
    private void processProductInfoChangeMessage(JSONObject messageJSONObject) {
        // 提取出商品id
        Long productId = messageJSONObject.getLong("productId");

        // 调用商品信息服务的接口
        // 直接用注释模拟：getProductInfo?productId=1，传递过去
        // 商品信息服务，一般来说就会去查询数据库，去获取productId=1的商品信息，然后返回回来

        String productInfoJSON = "{\"id\": 5, \"name\": \"iphone7手机\", \"price\": 5599, \"pictureList\":\"a.jpg,b.jpg\", \"specification\": \"iphone7的规格\", \"service\": \"iphone7的售后服务\", \"color\": \"红色,白色,黑色\", \"size\": \"5.5\", \"shopId\": 1, \"modifiedTime\": \"2017-01-01 12:00:00\"}";
        ProductInfo productInfo = JSONObject.parseObject(productInfoJSON, ProductInfo.class);
        cacheService.saveProductInfo2LocalCache(productInfo);
        log.info("===== 获取刚保存到本地缓存的商品信息: {} =====",
                cacheService.getProductInfoFromLocalCache(productId));

        // 加入分布式锁的代码
        // 将数据直接写入redis缓存之前 应该先获取一个zk的分布式锁
        ZooKeeperSession zooKeeperSession = ZooKeeperSession.getInstance();
        zooKeeperSession.acquireDistributedLock(productId);

        // 获取到锁之后 先从redis中获取数据
        ProductInfo existedProductInfo = cacheService.getProductInfoFromReidsCache(productId);
        if (existedProductInfo != null) {
            // 比较当亲数据的时间版本比已经有数据的时间版本是新还是旧
            try {
                Date date = sdf.parse(productInfo.getModifiedTime());
                Date existedDate = sdf.parse(existedProductInfo.getModifiedTime());

                if (date.before(existedDate)) {
                    // 不进行更新
                    return;
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        log.info("current date[" + productInfo.getModifiedTime() + "] is before than existedDate .");
        cacheService.saveProductInfo2LocalCache(productInfo);
        cacheService.saveProductInfo2ReidsCache(productInfo);
        // 释放缓存
        zooKeeperSession.releaseDistributedLock(productId);
    }

    /**
     * 处理店铺信息变更的消息
     * @param messageJSONObject
     */
    private void processShopInfoChangeMessage(JSONObject messageJSONObject) {
        // 提取出商品id
        Long productId = messageJSONObject.getLong("productId");
        Long shopId = messageJSONObject.getLong("shopId");
        String shopInfoJSON = "{\"id\": 1, \"name\": \"小王的手机店\", \"level\": 5, \"goodCommentRate\":0.99}";
        ShopInfo shopInfo = JSONObject.parseObject(shopInfoJSON, ShopInfo.class);
        cacheService.saveShopInfo2LocalCache(shopInfo);
        log.info("===== 获取刚保存到本地缓存的店铺信息：{} =====", cacheService.getShopInfoFromLocalCache(shopId));
        cacheService.saveShopInfo2ReidsCache(shopInfo);
    }


}
