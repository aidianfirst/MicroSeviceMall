package com.tang.mall.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.tang.mall.common.to.mq.SeckillOrderTo;
import com.tang.mall.common.utils.R;
import com.tang.mall.common.vo.MemberResponseVo;
import com.tang.mall.seckill.feign.CouponFeignService;
import com.tang.mall.seckill.feign.ProductFeignService;
import com.tang.mall.seckill.interceptor.LoginUserInterceptor;
import com.tang.mall.seckill.service.SeckillService;
import com.tang.mall.seckill.to.SeckillSkuRedisTo;
import com.tang.mall.seckill.vo.SeckillSessionWithSkusVo;
import com.tang.mall.seckill.vo.SkuInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author aidianfirst
 * @create 2021/11/24 12:17
 */
@Slf4j
@Service
public class SeckillServiceImpl implements SeckillService {
    @Resource
    CouponFeignService couponFeignService;

    @Resource
    ProductFeignService productFeignService;

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Resource
    RedissonClient redissonClient;

    @Resource
    RabbitTemplate rabbitTemplate;

    private final String SESSION_CACHE_PREFIX = "seckill:sessions:";

    private final String SECKILL_CHARE_PREFIX = "seckill:skus";

    private final String SKU_STOCK_SEMAPHORE = "seckill:stock:";    //+商品随机码


    @Override
    public void uploadSeckillSkuLatest3Days() {
        // 最近3天要参与的秒杀场次
        R session = couponFeignService.getLatest3DaySession();
        if(session.get("code").equals(0)) {
            // 商品数据
            List<SeckillSessionWithSkusVo> data = session.getData(new TypeReference<List<SeckillSessionWithSkusVo>>() {});

            // redis缓存场次信息
            saveSessionInfos(data);
            // redis缓存关联商品信息
            saveSessionSkuInfos(data);
        }
    }

    @Override
    public List<SeckillSkuRedisTo> getCurrentSeckillSkus() {
        long time = System.currentTimeMillis();
        // 查询全部场次缓存
        Set<String> keys = stringRedisTemplate.keys(SESSION_CACHE_PREFIX + "*");
        for(String key : keys){
            // 拆分key，拿到时间
            // String key = SESSION_CACHE_PREFIX + startTime + "_" + endTime
            String replace = key.replace(SESSION_CACHE_PREFIX, "");
            String[] s = replace.split("_");
            Long start = Long.parseLong(s[0]);
            Long end = Long.parseLong(s[1]);
            // 现在时间处于当前场次
            if(time >= start && time <= end){
                // 从场次缓存list拿到所有的商品id
                List<String> range = stringRedisTemplate.opsForList().range(key, -100, 100);
                // 根据商品id进行hash查询
                BoundHashOperations<String, String, String> hashOps = stringRedisTemplate.boundHashOps(SECKILL_CHARE_PREFIX);
                List<String> list = hashOps.multiGet(range);

                if(list != null){
                    List<SeckillSkuRedisTo> collect = list.stream().map(item -> {
                        SeckillSkuRedisTo redisTo = JSON.parseObject((String) item, SeckillSkuRedisTo.class);
                        return redisTo;
                    }).collect(Collectors.toList());
                    return collect;
                }
                break;
            }
        }
        return null;
    }

    @Override
    public SeckillSkuRedisTo getSkuSeckillInfo(Long skuId) {
        // 获取全部缓存的商品信息
        BoundHashOperations<String, String, String> hashOps = stringRedisTemplate.boundHashOps(SECKILL_CHARE_PREFIX);
        Set<String> keys = hashOps.keys();

        if(keys != null && keys.size() > 0){
            // 正则匹配商品id
            String regex = "\\d-" + skuId;
            for(String key : keys){
                // 如果正则和当前商品id匹配
                if (Pattern.matches(regex,key)) {
                    // 从Redis中取出数据来
                    String redisValue = hashOps.get(key);
                    // 进行序列化
                    SeckillSkuRedisTo redisTo = JSON.parseObject(redisValue, SeckillSkuRedisTo.class);

                    // 获取时间，随机码根据是否符合场次进行返回
                    Long currentTime = System.currentTimeMillis();
                    Long startTime = redisTo.getStartTime();
                    Long endTime = redisTo.getEndTime();

                    // 如果当前时间大于等于秒杀活动开始时间并且要小于活动结束时间
                    if (currentTime >= startTime && currentTime <= endTime) {
                        return redisTo;
                    }
                    redisTo.setRandomCode(null);
                    return redisTo;
                }
            }
        }
        return null;
    }

    @Override
    public String kill(String killId, String key, Integer num) throws InterruptedException {
        long s1 = System.currentTimeMillis();
        //获取当前用户的信息
        MemberResponseVo user = LoginUserInterceptor.ThreadLocal.get();

        //1、获取当前秒杀商品的详细信息从Redis中获取
        BoundHashOperations<String, String, String> hashOps = stringRedisTemplate.boundHashOps(SECKILL_CHARE_PREFIX);
        String skuInfoValue = hashOps.get(killId);
        if (StringUtils.isEmpty(skuInfoValue)) {
            return null;
        }

        // (合法性效验)
        SeckillSkuRedisTo redisTo = JSON.parseObject(skuInfoValue, SeckillSkuRedisTo.class);

        Long startTime = redisTo.getStartTime();
        Long endTime = redisTo.getEndTime();
        Long currentTime = System.currentTimeMillis();

        // 判断当前这个秒杀请求是否在活动时间区间内(效验时间的合法性)
        if (currentTime >= startTime && currentTime <= endTime) {

            // 2、效验随机码和商品id
            // 通过killId在缓存中拿到数据，比对缓存数据和传入数据是否一致
            String randomCode = redisTo.getRandomCode();
            String skuId = redisTo.getPromotionSessionId() + "-" +redisTo.getSkuId();
            if (randomCode.equals(key) && skuId.equals(killId)) {

                // 3、验证购物数量是否合理和库存量是否充足
                Integer seckillLimit = redisTo.getSeckillLimit();

                // 获取信号量
                String seckillCount = stringRedisTemplate.opsForValue().get(SKU_STOCK_SEMAPHORE + randomCode);
                Integer count = Integer.valueOf(seckillCount);

                // 判断信号量是否大于0，即有库存，购买数量不能超过限制数，且购买数不能超过剩余库存
                if (count > 0 && num <= seckillLimit && num <= count ) {
                    // 4、验证这个人是否已经买过了（幂等性处理）,如果秒杀成功，就去占位。userId-sessionId-skuId
                    // SETNX 原子性处理
                    String redisKey = user.getId() + "-" + skuId;

                    // 设置自动过期(活动结束时间-当前时间)
                    Long ttl = endTime - currentTime;
                    // 设置该用户，返回值代表其是否已经秒杀过
                    Boolean aBoolean = stringRedisTemplate.opsForValue().setIfAbsent(redisKey, num.toString(), ttl, TimeUnit.MILLISECONDS);
                    if (aBoolean) {
                        // 占位成功说明从来没有买过,分布式锁(获取信号量)
                        RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + randomCode);
                        // 秒杀成功，快速下单，在规定100ms等待时间内获取定额信号量
                        boolean semaphoreCount = semaphore.tryAcquire(num, 100, TimeUnit.MILLISECONDS);
                        // 如果短时间拿到信号量，保证Redis中还有商品库存
                        if (semaphoreCount) {
                            // 创建订单号和订单信息发送给MQ
                            // 秒杀成功 快速下单 发送消息到 MQ 整个操作时间在 10ms 左右
                            String timeId = IdWorker.getTimeId();
                            SeckillOrderTo orderTo = new SeckillOrderTo();
                            orderTo.setOrderSn(timeId);
                            orderTo.setMemberId(user.getId());
                            orderTo.setNum(num);
                            orderTo.setPromotionSessionId(redisTo.getPromotionSessionId());
                            orderTo.setSkuId(redisTo.getSkuId());
                            orderTo.setSeckillPrice(redisTo.getSeckillPrice());

                            // 封装秒杀订单信息，通过队列进行消费，由order服务处理
                            rabbitTemplate.convertAndSend("order-event-exchange","order.seckill.order",orderTo);
                            long s2 = System.currentTimeMillis();
                            log.info("1耗时..." + (s2 - s1));
                            return timeId;
                        }
                    }
                }
            }
        }

        long s3 = System.currentTimeMillis();
        log.info("2耗时..." + (s3 - s1));
        return null;
    }

    /**
     * 缓存秒杀场次信息
     * @param sessions
     */
    private void saveSessionInfos(List<SeckillSessionWithSkusVo> sessions) {

        sessions.stream().forEach(session -> {

            //获取当前场次的开始和结束时间的时间戳
            long startTime = session.getStartTime().getTime();
            long endTime = session.getEndTime().getTime();

            //存入到Redis中的key
            String key = SESSION_CACHE_PREFIX + startTime + "_" + endTime;

            //幂等性，判断Redis中是否有该信息，如果没有才进行添加
            Boolean hasKey = stringRedisTemplate.hasKey(key);
            //缓存场次信息
            if (!hasKey) {
                //获取到场次中所有商品的skuId
                List<String> skuIds = session.getRelationSkus().stream()
                        .map(item -> item.getPromotionSessionId() + "-" + item.getSkuId().toString()).collect(Collectors.toList());
                stringRedisTemplate.opsForList().leftPushAll(key,skuIds);
            }
        });
    }

    /**
     * 缓存秒杀场次所关联的商品信息
     * @param sessions
     */
    private void saveSessionSkuInfos(List<SeckillSessionWithSkusVo> sessions) {

        sessions.stream().forEach(session -> {
            //准备hash操作，绑定hash
            BoundHashOperations<String, Object, Object> operations = stringRedisTemplate.boundHashOps(SECKILL_CHARE_PREFIX);
            session.getRelationSkus().stream().forEach(seckillSkuVo -> {
                //生成随机码，商品存在则不添加，幂等性
                String token = UUID.randomUUID().toString().replace("-", "");
                String redisKey = seckillSkuVo.getPromotionSessionId().toString() + "-" + seckillSkuVo.getSkuId().toString();

                if (!operations.hasKey(redisKey)) {
                    //缓存商品信息
                    SeckillSkuRedisTo redisTo = new SeckillSkuRedisTo();

                    //1、先查询sku的基本信息，调用远程服务
                    R info = productFeignService.info(seckillSkuVo.getSkuId());
                    if (info.get("code").equals(0)) {
                        SkuInfoVo skuInfo = info.getData("skuInfo",new TypeReference<SkuInfoVo>(){});
                        redisTo.setSkuInfo(skuInfo);
                    }

                    //2、sku的秒杀信息
                    BeanUtils.copyProperties(seckillSkuVo,redisTo);

                    //3、设置当前商品的秒杀时间信息
                    redisTo.setStartTime(session.getStartTime().getTime());
                    redisTo.setEndTime(session.getEndTime().getTime());

                    //4、设置商品的随机码（防止恶意攻击）
                    redisTo.setRandomCode(token);

                    // 将信息序列化成json格式，进行Redis缓存
                    String seckillValue = JSON.toJSONString(redisTo);
                    operations.put(redisKey, seckillValue);

                    //5、使用库存作为分布式Redisson信号量（限流）
                    // 使用库存作为分布式信号量
                    RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + token);
                    // 商品可以秒杀的数量作为信号量
                    semaphore.trySetPermits(seckillSkuVo.getSeckillCount());
                }
            });
        });
    }
}
