package com.product.eshop.dao.redis;

/**
 *  * redis本身有各种各样的api和功能
 *  * 可以做出来很多很多非常花哨的功能，但是我们这个课程，是有侧重点的，不是讲解redis基础知识的
 *  * 是讲解大规模缓存架构
 * @author yangqian
 * @date 2019/8/28
 */
public interface RedisDao {

    void set(String key, String value);
    String get(String key);
    void delete(String key);

}