package com.tang.mall.ware.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author aidianfirst
 * @create 2021/11/7 21:08
 */
@FeignClient(value = "mall-product")
public interface ProductFeignService {

    @RequestMapping("/product/skuinfo/getSkuName")
    String getSkuName(@RequestParam("skuId") Long skuId);
}
