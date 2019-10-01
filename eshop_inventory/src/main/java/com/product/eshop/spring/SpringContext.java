package com.product.eshop.spring;

import org.springframework.context.ApplicationContext;

/**
 * @author yangqian
 * @date 2019/10/1
 */
public class SpringContext {

    private static ApplicationContext applicationContext;

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static void setApplicationContext(ApplicationContext applicationContext) {
        SpringContext.applicationContext = applicationContext;
    }

}
