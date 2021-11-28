package com.tang.mall.seckill.service;

import com.tang.mall.seckill.to.SeckillSkuRedisTo;

import java.util.List;

/**
 * @author aidianfirst
 * @create 2021/11/24 12:17
 */
public interface SeckillService {
    void uploadSeckillSkuLatest3Days();

    List<SeckillSkuRedisTo> getCurrentSeckillSkus();

    SeckillSkuRedisTo getSkuSeckillInfo(Long skuId);

    String kill(String killId, String key, Integer num) throws InterruptedException;
}
