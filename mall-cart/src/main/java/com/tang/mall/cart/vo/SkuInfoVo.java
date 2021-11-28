package com.tang.mall.cart.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author aidianfirst
 * @create 2021/11/18 19:47
 */
@Data
public class SkuInfoVo {
    private Long skuId;

    private Long spuId;

    private String skuName;

    private String skuDesc;

    private Long catelogId;

    private Long brandId;

    private String skuDefaultImg;

    private String skuTitle;

    private String skuSubtitle;

    private BigDecimal price;

    private Long saleCount;
}
