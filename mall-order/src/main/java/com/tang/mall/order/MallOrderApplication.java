package com.tang.mall.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author aidianfirst
 * @create 2021/10/29 15:31
 */
@MapperScan("com.tang.mall.order.dao")
@SpringBootApplication
@EnableDiscoveryClient
public class MallOrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(MallOrderApplication.class, args);
    }
}
