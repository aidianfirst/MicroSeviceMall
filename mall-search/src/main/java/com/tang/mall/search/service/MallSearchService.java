package com.tang.mall.search.service;

import com.tang.mall.search.vo.SearchParam;
import com.tang.mall.search.vo.SearchRespVo;

import java.io.IOException;

/**
 * @author aidianfirst
 * @create 2021/11/13 15:07
 */
public interface MallSearchService {

    SearchRespVo search(SearchParam searchParam) throws IOException;
}
