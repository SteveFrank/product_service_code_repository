package com.product.eshop.service;

import com.product.eshop.request.Request;

/**
 * 请求接口
 *
 * 用于异步处理service的实现
 *
 * 也是一个路由到对应处理逻辑的接口
 * @author yangqian
 * @date 2019/8/28
 */
public interface RequestAsyncProcessService {

    public void process(Request request);

}
