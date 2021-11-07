package com.tang.mall;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author aidianfirst
 * @create 2021/10/31 20:56
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableDiscoveryClient
public class MallThirdApplication {
    public static void main(String[] args) {
        SpringApplication.run(MallThirdApplication.class, args);
    }
}
