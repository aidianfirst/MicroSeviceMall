package com.tang.mall.product.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * @author aidianfirst
 * @create 2021/11/12 15:57
 */
@Configuration
public class MyRedissonConfig {
    // 通过RedissonClient使用Redisson
    @Bean(destroyMethod="shutdown")
    public RedissonClient redisson() throws IOException {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://192.168.56.11:6379");
        RedissonClient redisson = Redisson.create(config);
        return redisson;
    }
}
