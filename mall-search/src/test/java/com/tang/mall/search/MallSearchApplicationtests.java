package com.tang.mall.search;

import com.alibaba.fastjson.JSON;
import com.tang.mall.search.config.ElasticsearchConfig;
import lombok.Data;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * @author aidianfirst
 * @create 2021/11/8 22:43
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MallSearchApplication.class)
public class MallSearchApplicationtests {
    @Resource
    private RestHighLevelClient client;

    @Test
    public void test() throws IOException {
        IndexRequest indexRequest = new IndexRequest("admin");
        indexRequest.id("1");
        User user = new User();
        user.setName("111");
        user.setAge(12);
        String jsonString = JSON.toJSONString(user);
        indexRequest.source(jsonString, XContentType.JSON);

        IndexResponse index = client.index(indexRequest, ElasticsearchConfig.COMMON_OPTIONS);
        System.out.println(index);
    }

    @Data
    class User{
        private String name;
        private Integer age;
    }
}
