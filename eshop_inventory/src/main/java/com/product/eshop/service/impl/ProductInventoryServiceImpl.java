package com.product.eshop.service.impl;

import com.product.eshop.dao.redis.RedisDao;
import com.product.eshop.mapper.ProductInventoryMapper;
import com.product.eshop.model.ProductInventory;
import com.product.eshop.service.ProductInventoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author yangqian
 * @date 2019/8/28
 */
@Slf4j
@Service("productInventoryService")
public class ProductInventoryServiceImpl implements ProductInventoryService {

    @Resource
    private ProductInventoryMapper productInventoryMapper;
    @Resource
    private RedisDao redisDao;

    @Override
    public void updateProductInventory(ProductInventory productInventory) {
        productInventoryMapper.updateProductInventory(productInventory);
        log.info("prouductId: {}, 更新数据库中的数据",productInventory.getProductId());
    }

    @Override
    public void removeProductInventoryCache(ProductInventory productInventory) {
        String key = "product:inventory:" + productInventory.getProductId();
        redisDao.delete(key);
        log.info("productId: {}, 删除缓存", productInventory.getProductId());
    }

    /**
     * 根据商品id查询商品库存
     * @param productId 商品id
     * @return 商品库存
     */
    @Override
    public ProductInventory findProductInventory(Integer productId) {
        return productInventoryMapper.findProductInventory(productId);
    }

    /**
     * 设置商品库存的缓存
     * @param productInventory 商品库存
     */
    @Override
    public void setProductInventoryCache(ProductInventory productInventory) {
        String key = "product:inventory:" + productInventory.getProductId();
        redisDao.set(key, String.valueOf(productInventory.getInventoryCnt()));
    }

    /**
     * 获取商品库存的缓存
     * @param productId
     * @return
     */
    @Override
    public ProductInventory getProductInventoryCache(Integer productId) {
        Long inventoryCnt = 0L;

        String key = "product:inventory:" + productId;
        String result = redisDao.get(key);

        if(result != null && !"".equals(result)) {
            try {
                inventoryCnt = Long.valueOf(result);
                return new ProductInventory(productId, inventoryCnt);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

}
