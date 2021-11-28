package com.tang.mall.search.service;

import com.tang.mall.common.to.es.SkuEsModel;

import java.io.IOException;
import java.util.List;

/**
 * @author aidianfirst
 * @create 2021/11/9 23:12
 */
public interface ProductSaveService {

    void productStatusUp(List<SkuEsModel> models) throws IOException;
}
