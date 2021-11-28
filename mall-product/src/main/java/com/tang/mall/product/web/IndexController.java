package com.tang.mall.product.web;

import com.tang.mall.product.entity.CategoryEntity;
import com.tang.mall.product.service.CategoryService;
import com.tang.mall.product.vo.CateJsonVo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author aidianfirst
 * @create 2021/11/10 20:55
 */
@Controller
public class IndexController {
    @Resource
    CategoryService categoryService;

    @GetMapping({"index.html", "/"})
    public ModelAndView indexPage(ModelAndView modelAndView) {
        List<CategoryEntity> categoryEntities = categoryService.getLevel1Menu();
        modelAndView.addObject("categorys", categoryEntities);
        modelAndView.setViewName("index");
        return modelAndView;
    }

    @GetMapping("/index/json/catlog.json")
    @ResponseBody
    public Map<String, List<CateJsonVo>> getCateJson() {
        Map<String, List<CateJsonVo>> cateJsonMap = categoryService.getCateJson();
        return cateJsonMap;
    }
}
