package com.product.eshop.dao.redis.impl;

import com.product.eshop.dao.redis.RedisDao;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.JedisCluster;

import javax.annotation.Resource;

/**
 * @author yangqian
 * @date 2019/8/28
 */
@Repository("redisDao")
public class RedisDaoImpl implements RedisDao {

    @Resource
    private JedisCluster jedisCluster;

    @Override
    public void set(String key, String value) {
        jedisCluster.set(key, value);
    }

    @Override
    public String get(String key) {
        return jedisCluster.get(key);
    }

    @Override
    public void delete(String key) {
        jedisCluster.del(key);
    }

}
