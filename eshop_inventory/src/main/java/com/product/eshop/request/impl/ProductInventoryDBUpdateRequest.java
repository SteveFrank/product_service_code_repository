package com.product.eshop.request.impl;

import com.product.eshop.model.ProductInventory;
import com.product.eshop.request.Request;
import com.product.eshop.service.ProductInventoryService;

/**
 *
 * 比如商品发生了交易 需要修改商品对应的库存
 * 此时就会发送请求过来 要求更新呢库存 那么这里就是所谓 data update request 数据更新请求
 * * cache aside pattern
 * * (1) 删除缓存
 * * (2) 更新数据库
 * @author yangqian
 * @date 2019/8/28
 */
public class ProductInventoryDBUpdateRequest implements Request {

    /**
     * 商品库存
     */
    private ProductInventory productInventory;
    /**
     * 商品库存Service
     */
    private ProductInventoryService productInventoryService;

    public ProductInventoryDBUpdateRequest(ProductInventory productInventory,
                                           ProductInventoryService productInventoryService) {
        this.productInventory = productInventory;
        this.productInventoryService = productInventoryService;
    }

    @Override
    public void process() {
        // 删除redis中的缓存
        productInventoryService.removeProductInventoryCache(productInventory);
        // 修改数据库中的库存
        productInventoryService.updateProductInventory(productInventory);
    }

    /**
     * 获取商品id
     */
    @Override
    public Integer getProductId() {
        return productInventory.getProductId();
    }
}
