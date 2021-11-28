package com.tang.mall.product.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


/**
 * @author aidianfirst
 * @create 2021/11/9 22:54
 */
@FeignClient("mall-ware")
public interface WareFeignService {

    @PostMapping("/ware/waresku/hasstock")
    Boolean getSkuHasStock(@RequestParam("skuId") Long skuId);
}
