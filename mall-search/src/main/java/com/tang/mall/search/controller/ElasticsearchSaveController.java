package com.tang.mall.search.controller;

import com.tang.mall.common.exception.CodeEnum;
import com.tang.mall.common.to.es.SkuEsModel;
import com.tang.mall.common.utils.R;
import com.tang.mall.search.service.ProductSaveService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

/**
 * @author aidianfirst
 * @create 2021/11/9 23:09
 */
@RequestMapping("/search/save")
@RestController
public class ElasticsearchSaveController {
    @Resource
    ProductSaveService productSaveService;

    @PostMapping("/product")
    public R productStatusUp(@RequestBody List<SkuEsModel> models) throws IOException {
        productSaveService.productStatusUp(models);
        return R.ok();
    }
}
