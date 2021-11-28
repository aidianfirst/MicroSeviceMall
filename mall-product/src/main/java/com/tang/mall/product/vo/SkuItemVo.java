package com.tang.mall.product.vo;

import com.tang.mall.product.entity.SkuImagesEntity;
import com.tang.mall.product.entity.SkuInfoEntity;
import com.tang.mall.product.entity.SpuInfoDescEntity;
import lombok.Data;

import java.util.List;

/**
 * @author aidianfirst
 * @create 2021/11/15 15:13
 */
@Data
public class SkuItemVo {
    // 1. skuInfo 基本信息 `pms_sku_info`
    private SkuInfoEntity skuInfoEntity;
    // 是否有货
    private Boolean hasStock = true;
    // 2. skuImg  `pms_sku_imges`
    private List<SkuImagesEntity> skuImages;
    // 3. spu 销售属性组合
    private List<SkuItemSaleAttrVo> spuSaleAttrs;
    // 4. spu 的介绍 `pms_spu_desc`
    private SpuInfoDescEntity spuDesc;
    // 5. spu 规格参数
    private List<SpuItemAttrGroupVo> spuAttrGroupVos;
    //6、秒杀商品的优惠信息
    private SeckillSkuVo seckillSkuVo;
}

