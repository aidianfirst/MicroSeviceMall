package com.tang.mall.search.feign;

import com.tang.mall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author aidianfirst
 * @create 2021/11/14 20:45
 */
@FeignClient("mall-product")
public interface ProductFeignService {

    @GetMapping("/product/attr/info/{attrId}")
    R info(@PathVariable("attrId") Long attrId);

    @GetMapping("/product/brand/infos")
    R getBrandByIds(@RequestParam("brandIds") List<Long> brandIds);

    @GetMapping("/product/category/getCateInfo")
    R getCateInfo(@RequestParam("catId") Long catId);
}
