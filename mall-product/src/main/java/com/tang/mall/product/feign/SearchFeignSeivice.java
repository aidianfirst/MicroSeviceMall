package com.tang.mall.product.feign;

import com.tang.mall.common.to.es.SkuEsModel;
import com.tang.mall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author aidianfirst
 * @create 2021/11/9 23:29
 */
@FeignClient("mall-search")
public interface SearchFeignSeivice {

    @PostMapping("/search/save/product")
    R productStatusUp(@RequestBody List<SkuEsModel> models);
}
