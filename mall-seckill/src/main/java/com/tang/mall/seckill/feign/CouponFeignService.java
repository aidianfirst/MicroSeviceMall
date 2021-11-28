package com.tang.mall.seckill.feign;

import com.tang.mall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author aidianfirst
 * @create 2021/11/24 12:19
 */
@FeignClient("mall-coupon")
public interface CouponFeignService {

    @GetMapping(value = "/coupon/seckillsession/Lates3DaySession")
    R getLatest3DaySession();
}
