package com.tang.mall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.tang.mall.common.to.es.SkuEsModel;
import com.tang.mall.search.config.ElasticsearchConfig;
import com.tang.mall.search.constant.EsConstant;
import com.tang.mall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

/**
 * @author aidianfirst
 * @create 2021/11/9 23:14
 */
@Slf4j
@Service
public class ProductSaveServiceImpl implements ProductSaveService {
    @Resource
    RestHighLevelClient restHighLevelClient;

    @Override
    public void productStatusUp(List<SkuEsModel> models) throws IOException {
        // 数据保存到es
        BulkRequest bulkRequest = new BulkRequest();
        for(SkuEsModel model : models){
            IndexRequest indexRequest = new IndexRequest(EsConstant.PRODUCT_INDEX);
            indexRequest.id(model.getSkuId().toString());
            String s = JSON.toJSONString(model);
            indexRequest.source(s, XContentType.JSON);
            bulkRequest.add(indexRequest);
        }

        BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, ElasticsearchConfig.COMMON_OPTIONS);

        BulkItemResponse[] items = bulk.getItems();

        for (BulkItemResponse item : items) {
            log.info("{} 上架成功的" + item.getId());
        }
        if (bulk.hasFailures()) {
            throw new RuntimeException("上架出现异常");
        }
    }
}
