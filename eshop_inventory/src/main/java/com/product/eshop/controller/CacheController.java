package com.product.eshop.controller;

import com.product.eshop.model.ProductInfo;
import com.product.eshop.service.CacheService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

/**
 * @author yangqian
 * @date 2019/10/1
 */
@Controller
public class CacheController {

    @Resource
    private CacheService cacheService;

    @RequestMapping("/testPutCache")
    @ResponseBody
    public ProductInfo testPutCache(ProductInfo productInfo) {
        return cacheService.saveLocalCache(productInfo);
    }

    @RequestMapping("/testGetCache")
    @ResponseBody
    public ProductInfo testGetCache(Long id) {
        return cacheService.getLocalCache(id);
    }

}
