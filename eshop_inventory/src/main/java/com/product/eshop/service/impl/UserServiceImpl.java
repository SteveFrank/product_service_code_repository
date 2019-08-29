package com.product.eshop.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.product.eshop.mapper.UserMapper;
import com.product.eshop.model.User;
import com.product.eshop.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisCluster;

import javax.annotation.Resource;

/**
 * @author yangqian
 * @date 2019/8/27
 */
@Slf4j
@Service("userService")
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;
    @Resource
    private JedisCluster jedisCluster;

    @Override
    public User findUserInfo() {
        return userMapper.findUserInfo();
    }

    @Override
    public User getCachedUserInfo() {
        User user = userMapper.findUserInfo();
        jedisCluster.set("cached_user_lisi", JSON.toJSONString(user));
        String userJSON = jedisCluster.get("cached_user_lisi");
        log.info("user : {}", userJSON);
        return JSONObject.parseObject(userJSON, User.class);
    }
}
