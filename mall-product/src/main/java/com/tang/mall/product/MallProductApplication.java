package com.tang.mall.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * @author aidianfirst
 * @create 2021/10/28 22:53
 */
@MapperScan("com.tang.mall.product.dao")
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.tang.mall.product.feign")
@EnableRedisHttpSession
public class MallProductApplication {
    public static void main(String[] args) {
        SpringApplication.run(MallProductApplication.class, args);
    }
}
