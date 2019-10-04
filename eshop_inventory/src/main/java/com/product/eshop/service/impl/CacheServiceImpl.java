package com.product.eshop.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.product.eshop.model.ProductInfo;
import com.product.eshop.model.ShopInfo;
import com.product.eshop.service.CacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisCluster;

import javax.annotation.Resource;

/**
 * 缓存Service的实现类
 * @author yangqian
 * @date 2019/10/1
 */
@Slf4j
@Service("cacheService")
public class CacheServiceImpl implements CacheService {

    @Resource
    private JedisCluster jedisCluster;

    public static final String CACHE_NAME = "local";

    /**
     * 将商品保存到本地缓存中
     * @param productInfo 商品信息
     * @return
     */
    @CachePut(value = CACHE_NAME, key = "'key_' + #productInfo.getId()")
    @Override
    public ProductInfo saveLocalCache(ProductInfo productInfo) {
        return productInfo;
    }

    /**
     * 从本地缓存中获取到商品信息
     * @param id
     * @return
     */
    @Cacheable(value = CACHE_NAME, key = "'key_' + #id")
    @Override
    public ProductInfo getLocalCache(Long id) {
        return null;
    }

    /**
     * 将商品信息保存到本地的ehcache缓存中
     * @param productInfo
     */
    @CachePut(value = CACHE_NAME, key = "'product_info_'+#productInfo.getId()")
    @Override
    public ProductInfo saveProductInfo2LocalCache(ProductInfo productInfo) {
        return productInfo;
    }

    /**
     * 从本地ehcache缓存中获取商品信息
     * @param productId
     * @return
     */
    @Cacheable(value = CACHE_NAME, key = "'product_info_'+#productId")
    @Override
    public ProductInfo getProductInfoFromLocalCache(Long productId) {
        return null;
    }

    /**
     * 将商品信息保存到redis中
     * @param productInfo
     */
    @Override
    public void saveProductInfo2ReidsCache(ProductInfo productInfo) {
        String key = "product_info_" + productInfo.getId();
        jedisCluster.set(key, JSONObject.toJSONString(productInfo));
    }

    @Override
    @CachePut(value = CACHE_NAME, key = "'shop_info_'+#shopInfo.getId()")
    public ShopInfo saveShopInfo2LocalCache(ShopInfo shopInfo) {
        return null;
    }

    @Override
    @Cacheable(value = CACHE_NAME, key = "'shop_info_'+#shopId")
    public ShopInfo getShopInfoFromLocalCache(Long shopId) {
        return null;
    }

    @Override
    public void saveShopInfo2ReidsCache(ShopInfo shopInfo) {
        String key = "shop_info_" + shopInfo.getId();
        jedisCluster.set(key, JSONObject.toJSONString(shopInfo));
    }

    /**
     * 从redis中获取商品信息
     * @param productId
     */
    @Override
    public ProductInfo getProductInfoFromReidsCache(Long productId) {
        String key = "product_info_" + productId;
        String json = jedisCluster.get(key);
        if(json != null) {
            return JSONObject.parseObject(json, ProductInfo.class);
        }
        return null;
    }

    /**
     * 从redis中获取店铺信息
     * @param shopId
     */
    @Override
    public ShopInfo getShopInfoFromReidsCache(Long shopId) {
        String key = "shop_info_" + shopId;
        String json = jedisCluster.get(key);
        if(json != null) {
            return JSONObject.parseObject(json, ShopInfo.class);
        }
        return null;
    }
}
