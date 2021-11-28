package com.tang.mall.search.vo;

import lombok.Data;

import java.util.List;

/**
 * @author aidianfirst
 * @create 2021/11/13 15:06
 */
@Data
public class SearchParam {
    // 检索关键字
    private String keyword;
    //三级分类id
    private Long catelog3Id;
    /**
     * 排序
     * sort = saleCount_Asc/Desc
     * sort = skuPrice_Asc
     * sort = hotScore_Asc
     */
    private String sort;

    /**
     * 过滤
     * hasStock = 0无 / 1有
     * skuPrice = 1_500/_500/500_
     * brandIds = 1,2
     * attr=1_其他&attr=2_3寸
     */
    private Integer hasStock;
    private String skuPrice;
    private List<Long> brandId;
    private List<String> attrs;

    // 页码 不传默认等于1
    private Integer pageNum = 1;
    // 原生请求路径
    private String _queryString;

}
