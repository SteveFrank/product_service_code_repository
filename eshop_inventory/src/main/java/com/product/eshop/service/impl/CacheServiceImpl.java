package com.product.eshop.service.impl;

import com.product.eshop.model.ProductInfo;
import com.product.eshop.service.CacheService;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * 缓存Service的实现类
 * @author yangqian
 * @date 2019/10/1
 */
@Service("cacheService")
public class CacheServiceImpl implements CacheService {

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

}
