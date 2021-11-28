package com.tang.mall.product.vo;

import lombok.Data;

import java.util.List;

/**
 * @author aidianfirst
 * @create 2021/11/10 21:07
 */
@Data
public class CateJsonVo {
    /**
     * 一级分类的id
     */
    private Long catelog1Id;
    /**
     * 3级分类列表
     */
    private List<Catelog3List> catelog3List;
    /**
     * 二级分类id
     */
    private Long id;
    /**
     * 名字
     */
    private String name;

    @Data
    public static class Catelog3List {
        /**
         * 2级分类id
         */
        private Long catelog2Id;
        /**
         * 三级分类id
         */
        private Long id;
        /**
         * 名字
         */
        private String name;
    }
}
