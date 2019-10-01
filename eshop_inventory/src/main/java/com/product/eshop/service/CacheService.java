package com.product.eshop.service;

import com.product.eshop.model.ProductInfo;

/**
 * @author yangqian
 * @date 2019/10/1
 */
public interface CacheService {

    /**
     * 将商品信息保存到本地缓存中
     * @param productInfo
     * @return
     */
    public ProductInfo saveLocalCache(ProductInfo productInfo);

    /**
     * 从本地缓存中获取到商品信息
     * @param id
     * @return
     */
    public ProductInfo getLocalCache(Long id);

}
