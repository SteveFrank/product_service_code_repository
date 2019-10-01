package com.product.eshop.controller;

import com.product.eshop.model.ProductInventory;
import com.product.eshop.request.Request;
import com.product.eshop.request.impl.ProductInventoryCacheRefreshRequest;
import com.product.eshop.request.impl.ProductInventoryDBUpdateRequest;
import com.product.eshop.service.ProductInventoryService;
import com.product.eshop.service.RequestAsyncProcessService;
import com.product.eshop.vo.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

/**
 * @author yangqian
 * @date 2019/8/28
 */
@Slf4j
@Controller
public class ProductInventoryController {

    @Resource
    private RequestAsyncProcessService requestAsyncProcessService;
    @Resource
    private ProductInventoryService productInventoryService;

    /**
     * 更新商品库存
     */
    @RequestMapping("/updateProductInventory")
    @ResponseBody
    public Response updateProductInventory(ProductInventory productInventory) {
        log.info("接受到更新商品库存的请求 ... ...");
        Response response = null;
        try {
            Request request = new ProductInventoryDBUpdateRequest(
                    productInventory, productInventoryService);
            // 直接放入到处理队列中
            requestAsyncProcessService.process(request);
            response = new Response(Response.SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            response = new Response(Response.FAILURE);
        }

        return response;
    }

    /**
     * 获取商品库存
     */
    @RequestMapping("/getProductInventory")
    @ResponseBody
    public ProductInventory getProductInventory(Integer productId) {
        ProductInventory productInventory = null;

        try {
            Request request = new ProductInventoryCacheRefreshRequest(
                    productId, productInventoryService);
            requestAsyncProcessService.process(request);
            // 将请求扔给service异步去处理以后，就需要while(true)一会儿，在这里hang住
            // 去尝试等待前面有商品库存更新的操作，同时缓存刷新的操作，将最新的数据刷新到缓存中
            long startTime = System.currentTimeMillis();
            long endTime = 0L;
            long waitTime = 0L;
            // 等待超过200ms没有从缓存中获取到结果 则直接通过数据库中的数据返回获取
            // 正式开发中需要多次压测来进行时间的配置 不要 [hard code]
            while (waitTime <= 200) {
                // 尝试去redis中读取一次商品库存的缓存数据
                productInventory = productInventoryService.getProductInventoryCache(productId);
                if (productInventory != null) {
                    // 如果读取到了结果，那么就返回
                    return productInventory;
                } else {
                    // 如果没有读取到结果，那么等待一段时间
                    Thread.sleep(20);
                    endTime = System.currentTimeMillis();
                    waitTime = endTime - startTime;
                }
            }
            // 直接尝试从数据库中读取数据
            productInventory = productInventoryService.findProductInventory(productId);
            if(productInventory != null) {
                // 将缓存刷新
                productInventoryService.setProductInventoryCache(productInventory);
                return productInventory;
            }
        } catch (Exception e) {
            log.error("获取商品库失败 ... ...", e);
        }
        // 上述流程全部失败 直接返回一个数据 避免前端一直等待请求返回结果
        return new ProductInventory(productId, -1L);
    }

}
