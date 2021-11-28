package com.tang.mall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.tang.mall.common.to.es.SkuEsModel;
import com.tang.mall.common.utils.R;
import com.tang.mall.search.config.ElasticsearchConfig;
import com.tang.mall.search.constant.EsConstant;
import com.tang.mall.search.feign.ProductFeignService;
import com.tang.mall.search.service.MallSearchService;
import com.tang.mall.search.vo.*;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author aidianfirst
 * @create 2021/11/13 15:08
 */
@Service
public class MallSearchServiceImpl implements MallSearchService {
    @Resource
    RestHighLevelClient client;

    @Resource
    ProductFeignService productFeignService;

    /**
     * 根据检索条件去es查询
     */
    @Override
    public SearchRespVo search(SearchParam searchParam) throws IOException {
        SearchRequest searchRequest = buildSearchRequest(searchParam);
        SearchResponse response = client.search(searchRequest, ElasticsearchConfig.COMMON_OPTIONS);
        // 封装响应数据 成SearchRespVo
        SearchRespVo searchRespVo = buildSearchResult(response, searchParam);
        return searchRespVo;
    }

    /**
     * 构建请求dsl语句，进行检索
     */
    private SearchRequest buildSearchRequest(SearchParam searchParam) {

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        /**
         * 模糊匹配 | 聚合 | 高亮 | 排序 | 分页 | 过滤
         */
        // bool
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        /**
         *  1.1 bool : must 模糊匹配
         */
        if (!StringUtils.isEmpty(searchParam.getKeyword())) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("skuTitle", searchParam.getKeyword()));
        }

        /**
         *  1.2 bool : filter 过滤
         */
        // 三级分类id过滤
        if (searchParam.getCatelog3Id() != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("catelogId", searchParam.getCatelog3Id()));
        }

        // 品牌id过滤 brandIds = 1,2,3
        if (searchParam.getBrandId() != null && searchParam.getBrandId().size() > 0) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId", searchParam.getBrandId()));
        }

        // 库存状态过滤 hasStock = 0无 / 1有
        if (searchParam.getHasStock() != null) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery("hasStock", searchParam.getHasStock() == 1));
        }

        // 价格区间过滤 skuPrice = 1_500 / _500 / 500_
        if (!StringUtils.isEmpty(searchParam.getSkuPrice())) {
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("skuPrice");
            String[] s = searchParam.getSkuPrice().split("_");
            if (s.length == 2) {
                rangeQuery.gte(s[0]).lt(s[1]);
            } else if (s.length == 1) {
                if (searchParam.getSkuPrice().startsWith("_")) {
                    rangeQuery.lt(s[0]);
                }
                if (searchParam.getSkuPrice().endsWith("_")) {
                    rangeQuery.gte(s[0]);
                }
            }
            boolQueryBuilder.filter(rangeQuery);
        }

        // 按照指定的属性过滤，多个属性循环封装 attr=1_其他&attr=2_3寸:4寸
        if (searchParam.getAttrs() != null && searchParam.getAttrs().size() > 0) {
            // 每一个属性都得生成一个nested,放入boolBuilder
            for (String attrStr : searchParam.getAttrs()) {
                BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
                String[] s = attrStr.split("_");
                String attrId = s[0];
                String[] attrValues = s[1].split(":");
                queryBuilder.must(QueryBuilders.termQuery("attrs.attrId", attrId));
                queryBuilder.must(QueryBuilders.termsQuery("attrs.attrValue", attrValues));

                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs", queryBuilder, ScoreMode.None);
                boolQueryBuilder.filter(nestedQuery);
            }
        }

        // 查询条件封装
        searchSourceBuilder.query(boolQueryBuilder);

        /** 2.1 排序
         *  sort = saleCount_Asc/Desc
         *  sort = skuPrice_Asc
         *  sort = hotScore_Asc
         */
        if (!StringUtils.isEmpty(searchParam.getSort())) {
            String[] s = searchParam.getSort().split("_");
            SortOrder sortOrder = s[1].equalsIgnoreCase("asc") ? SortOrder.ASC : SortOrder.DESC;
            searchSourceBuilder.sort(s[0], sortOrder);
        }

        /** 2.2 分页
         * pageNum =1 from: 0 size: 5 [0,1,2,3,4]
         * pageNum =2 from: 5 size: 5 [5,6,7,8,9]
         * from = (pageNum-1) * size
         */
        searchSourceBuilder.from((searchParam.getPageNum() - 1) * EsConstant.PRODUCT_PAGESIZE);
        searchSourceBuilder.size(EsConstant.PRODUCT_PAGESIZE);

        /**
         *  2.3 高亮
         */
        if (!StringUtils.isEmpty(searchParam.getKeyword())) {
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            // 前置标签
            highlightBuilder = highlightBuilder.field("skuTitle");
            highlightBuilder.preTags("<b style='color:red'>");
            // 后置标签
            highlightBuilder.postTags("</b>");
            searchSourceBuilder.highlighter(highlightBuilder);
        }

        /**
         * 2.4.1 : 聚合 brand
         */
        TermsAggregationBuilder brandAgg = AggregationBuilders.terms("brand_agg");
        TermsAggregationBuilder brandIdAgg = brandAgg.field("brandId").size(50);
        brandIdAgg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(10));
        brandIdAgg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(10));
        searchSourceBuilder.aggregation(brandAgg);

        /**
         * 2.4.2 ： 聚合 catelog
         */
        TermsAggregationBuilder catelogAgg = AggregationBuilders.terms("catelog_agg");
        TermsAggregationBuilder catelogIdAgg = catelogAgg.field("catelogId").size(20);
        catelogIdAgg.subAggregation(AggregationBuilders.terms("catelog_name_agg").field("catelogName").size(10));
        searchSourceBuilder.aggregation(catelogAgg);

        /**
         * 2.4.3 ： 聚合属性
         */
        NestedAggregationBuilder nestedAttrAgg = AggregationBuilders.nested("attr_agg", "attrs");
        TermsAggregationBuilder attrIdAgg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId").size(10);
        attrIdAgg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(10));
        attrIdAgg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(50));
        nestedAttrAgg.subAggregation(attrIdAgg);
        searchSourceBuilder.aggregation(nestedAttrAgg);

        String s = searchSourceBuilder.toString();
        System.out.println("DSL:" + s);

        SearchRequest searchRequest = new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, searchSourceBuilder);
        return searchRequest;
    }

    /**
     * 封装返回数据
     */
    private SearchRespVo buildSearchResult(SearchResponse response, SearchParam searchParam) {
        SearchRespVo searchRespVo = new SearchRespVo();
        // 获取dsl请求语句命中的记录
        SearchHits searchHits = response.getHits();

        /**
         * 封装分页信息
         */
        // 总记录数
        Long total = searchHits.getTotalHits().value;
        searchRespVo.setTotal(total);
        // 总页码，除不尽就多一页
        Integer totalPage = Math.toIntExact((total % EsConstant.PRODUCT_PAGESIZE == 0) ? (total / EsConstant.PRODUCT_PAGESIZE) : (total / EsConstant.PRODUCT_PAGESIZE + 1));
        searchRespVo.setTotalPage(totalPage);
        searchRespVo.setPageNum(searchParam.getPageNum());
        List<Integer> pageNavs = new ArrayList<>();
        for (Integer i = 1; i <= totalPage; i++) {
            pageNavs.add(i);
        }
        searchRespVo.setPageNavs(pageNavs);

        /**
         * 封装skuModel
         */
        SearchHit[] hits = searchHits.getHits();
        List<SkuEsModel> skuEsModelList = new ArrayList<>();
        if (hits != null && hits.length > 0) {
            for (SearchHit hit : hits) {
                String sourceAsString = hit.getSourceAsString();
                SkuEsModel skuEsModel = JSON.parseObject(sourceAsString, SkuEsModel.class);
                if (!StringUtils.isEmpty(searchParam.getKeyword())) {
                    // 显示高亮 TODO 有问题，highlightFields为null
                    Map<String, HighlightField> highlightFields = hit.getHighlightFields();
                    HighlightField highlight = highlightFields.get("skuTitle");
                    if (highlight != null) {
                        Text[] fragments = highlight.fragments();
                        String fragmentString = fragments[0].string();
                        System.out.println(fragmentString);
                        skuEsModel.setSkuTitle(fragmentString);
                    }
                }
                skuEsModelList.add(skuEsModel);
            }
        }
        searchRespVo.setProducts(skuEsModelList);

        /**
         * 封装 分类聚合信息
         */
        ParsedLongTerms catelog_agg = response.getAggregations().get("catelog_agg");
        List<SearchRespVo.CatelogVo> catelogVos = new ArrayList<>();
        for (Terms.Bucket bucket : catelog_agg.getBuckets()) {
            SearchRespVo.CatelogVo catelogVo = new SearchRespVo.CatelogVo();
            String idString = bucket.getKeyAsString();
            catelogVo.setCatelogId(Long.parseLong(idString));
            ParsedStringTerms catelog_name_agg = bucket.getAggregations().get("catelog_name_agg");
            String nameString = catelog_name_agg.getBuckets().get(0).getKeyAsString();
            catelogVo.setCatelogName(nameString);
            catelogVos.add(catelogVo);
        }
        searchRespVo.setCatelogVos(catelogVos);

        /**
         * 封装 品牌聚合信息
         */
        List<SearchRespVo.BrandVo> brandVos = new ArrayList<>();
        ParsedLongTerms brand_agg = response.getAggregations().get("brand_agg");
        for (Terms.Bucket bucket : brand_agg.getBuckets()) {
            SearchRespVo.BrandVo brandVo = new SearchRespVo.BrandVo();
            brandVo.setBrandId((Long) bucket.getKey());
            ParsedStringTerms brand_img_agg = bucket.getAggregations().get("brand_img_agg");
            brandVo.setBrandImg(brand_img_agg.getBuckets().get(0).getKeyAsString());
            ParsedStringTerms brand_name_agg = bucket.getAggregations().get("brand_name_agg");
            brandVo.setBrandName(brand_name_agg.getBuckets().get(0).getKeyAsString());
            brandVos.add(brandVo);
        }
        searchRespVo.setBrands(brandVos);

        /**
         *  封装 attrs属性信息
         */
        List<SearchRespVo.AttrVo> attrVos = new ArrayList<>();
        ParsedNested attr_agg = response.getAggregations().get("attr_agg");
        ParsedLongTerms attr_id_agg = attr_agg.getAggregations().get("attr_id_agg");
        for (Terms.Bucket bucket : attr_id_agg.getBuckets()) {
            SearchRespVo.AttrVo attrVo = new SearchRespVo.AttrVo();
            Long attrId = (Long) bucket.getKey();
            attrVo.setAttrId((attrId));
            ParsedStringTerms attr_name_agg = bucket.getAggregations().get("attr_name_agg");
            attrVo.setAttrName(attr_name_agg.getBuckets().get(0).getKeyAsString());
            ParsedStringTerms attr_value_agg = bucket.getAggregations().get("attr_value_agg");
            List<String> attrValues = new ArrayList<>();
            for (int i = 0; i < attr_value_agg.getBuckets().size(); i++) {
                attrValues.add(attr_value_agg.getBuckets().get(i).getKeyAsString());
            }
            attrVo.setAttrValue(attrValues);
            attrVos.add(attrVo);
        }
        searchRespVo.setAttrs(attrVos);

        /**
         * 构建面包屑导航
         * 属性面包屑导航
         */
        List<String> attrs = searchParam.getAttrs();
        List<SearchRespVo.NavVo> navVoList = new ArrayList<>();
        if (attrs != null && attrs.size() > 0) {
            navVoList = attrs.stream().map(item -> {
                SearchRespVo.NavVo navVo = new SearchRespVo.NavVo();
                String[] s = item.split("_");
                // 远程查出attrName
                R r = productFeignService.info(Long.parseLong(s[0]));

                searchRespVo.getAttrIds().add(Long.parseLong(s[0]));
                if (r.get("code").equals(0)) {
                    AttrResponseVo attrResponseVo = r.getData("attr", new TypeReference<AttrResponseVo>() {});
                    navVo.setNavName(attrResponseVo.getAttrName());
                } else {
                    navVo.setNavName("");
                }
                // 取消查询条件后url跳转逻辑
                String nQueryString = replaceUrl(searchParam, item, "attrs");
                navVo.setLink("http://search.mall.com/list.html?" + nQueryString);
                navVo.setNavValue(s[1]);
                return navVo;
            }).collect(Collectors.toList());
        }
        searchRespVo.setNavs(navVoList);

        /**
         * 品牌面包屑导航
         */
        if (searchParam.getBrandId() != null && searchParam.getBrandId().size() > 0) {
            navVoList = searchRespVo.getNavs();
            SearchRespVo.NavVo navVo = new SearchRespVo.NavVo();
            // 远程查询品牌
            R r = productFeignService.getBrandByIds(searchParam.getBrandId());
            if (r.get("code").equals(0)) {
                List<BrandVo> brands = r.getData("brands", new TypeReference<List<BrandVo>>() {});
                StringBuffer buffer = new StringBuffer();
                String replace = "";
                for (int i = 0; i < brands.size(); i++) {
                    BrandVo brandVo = brands.get(i);
                    if (i == 0) {
                        buffer.append(brandVo.getName());
                    } else {
                        buffer.append(brandVo.getName() + ";");
                    }
                    replace = replaceUrl(searchParam, brandVo.getBrandId() + "", "brandId");
                }
                navVo.setNavName("");
                navVo.setNavValue(buffer.toString());
                navVo.setLink("http://search.mall.com/list.html?" + replace);
            }
            navVoList.add(navVo);
            searchRespVo.setNavs(navVoList);
        }

        /**
         * 分类面包屑导航
         */
        if (searchParam.getCatelog3Id() != null) {
            SearchRespVo.NavVo navVo = new SearchRespVo.NavVo();
            // 远程查询分类的值
            R r = productFeignService.getCateInfo(searchParam.getCatelog3Id());
            if (r.get("code").equals(0)) {
                CateVo category = r.getData("category", new TypeReference<CateVo>() {});
                navVo.setNavName("");
                navVo.setNavValue(category.getName());
            }
            if (navVo != null) {
                navVoList.add(navVo);
            }
            searchRespVo.setNavs(navVoList);
        }

        return searchRespVo;
    }

    /**
     * 构建特换url
     */
    private String replaceUrl(SearchParam searchParam, String item, String key) {
        String encode = "";
        try {
            encode = URLEncoder.encode(item, "utf-8");
            encode = encode.replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return searchParam.get_queryString().replace("&" + key + "=" + encode, "");
    }
}
