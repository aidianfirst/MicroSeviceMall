package com.tang.mall.product.web;

import com.tang.mall.product.service.SkuInfoService;
import com.tang.mall.product.vo.SkuItemVo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.util.concurrent.ExecutionException;

/**
 * @author aidianfirst
 * @create 2021/11/15 15:06
 */
@Controller
public class ItemController {
    @Resource
    SkuInfoService skuInfoService;

    @RequestMapping("/{skuId}.html")
    public String itemPage(@PathVariable("skuId") Long skuId, Model model) throws ExecutionException, InterruptedException {
        SkuItemVo skuItemVo = skuInfoService.itemInfo(skuId);
        model.addAttribute("item", skuItemVo);
        return "item";
    }
}
