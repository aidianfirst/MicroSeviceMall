package com.tang.mall.search.controller;

import com.tang.mall.search.service.MallSearchService;
import com.tang.mall.search.vo.SearchParam;
import com.tang.mall.search.vo.SearchRespVo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author aidianfirst
 * @create 2021/11/13 15:00
 */
@Controller
public class SearchController {
    @Resource
    MallSearchService mallSearchService;

    @GetMapping("/list.html")
    public String listPage(SearchParam searchParam, Model model, HttpServletRequest request) throws IOException {
        String queryString = request.getQueryString();
        searchParam.set_queryString(queryString);
        SearchRespVo result = mallSearchService.search(searchParam);
        model.addAttribute("result", result);
        return "list";
    }

    @GetMapping("/search.html")
    public String searchPage() {
        return "list";
    }
}
