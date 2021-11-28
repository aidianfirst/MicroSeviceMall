package com.tang.mall.order.feign;

import com.tang.mall.common.utils.R;
import com.tang.mall.order.vo.WareSkuLockVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * @author aidianfirst
 * @create 2021/11/20 18:39
 */
@FeignClient("mall-ware")
public interface WareFeignService {

    @PostMapping("/ware/waresku/hasstock")
    Boolean getSkuHasStock(@RequestParam("skuId") Long skuId);

    @GetMapping("/ware/wareinfo/fare/{addrId}")
    R getFare(@PathVariable("addrId") Long addrId);

    @PostMapping("/ware/waresku/lock/order")
    R orderLockStock(@RequestBody WareSkuLockVo vo);
}

