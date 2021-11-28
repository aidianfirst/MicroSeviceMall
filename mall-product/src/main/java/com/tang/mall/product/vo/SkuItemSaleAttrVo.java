package com.tang.mall.product.vo;

import lombok.Data;

import java.util.List;

/**
 * @author aidianfirst
 * @create 2021/11/15 15:14
 */
@Data
public class SkuItemSaleAttrVo {
    private Long attrId;
    private String attrName;
    private List<AttrValueWithSkuIdVo> attrValues;
}