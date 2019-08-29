package com.product.eshop.service;

import com.product.eshop.model.User;

/**
 * 用户接口服务
 * @author yangqian
 * @date 2019/8/27
 */
public interface UserService {

    /**
     * 查询用户信息
     * @return 用户信息
     */
    public User findUserInfo();

    /**
     * 查询redis中缓存的用户信息
     * @return
     */
    public User getCachedUserInfo();

}
