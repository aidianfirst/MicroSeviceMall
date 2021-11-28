package com.tang.mall.search.vo;

import com.tang.mall.common.to.es.SkuEsModel;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author aidianfirst
 * @create 2021/11/13 15:25
 */
@Data
public class SearchRespVo {
    // 所有商品信息
    private List<SkuEsModel> products;

    //当前页
    private Integer pageNum;
    // 总页码
    private Integer totalPage;
    // 总记录数
    private Long total;
    // 可遍历得导航页
    private List<Integer> pageNavs;

    // 品牌
    private List<BrandVo> brands;
    // 当前查询到的结果的所有属性
    private List<AttrVo> attrs;
    // 当前查询到的结果的分类
    private List<CatelogVo> catelogVos;

    // 面包屑导航数据
    private List<NavVo> navs = new ArrayList<>();
    private List<Long> attrIds = new ArrayList<>();

    @Data
    public static class NavVo {
        private String navName;
        private String navValue;
        // 跳转路径
        private String link;
    }

    @Data
    public static class BrandVo {
        private Long brandId;
        private String brandName;
        private String brandImg;
    }

    @Data
    public static class AttrVo {
        private Long attrId;
        private String attrName;
        private List<String> attrValue;
    }

    @Data
    public static class CatelogVo {
        private Long catelogId;
        private String catelogName;
    }
}
