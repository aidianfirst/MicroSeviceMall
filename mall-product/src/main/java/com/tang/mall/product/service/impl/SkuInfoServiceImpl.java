package com.tang.mall.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.sun.org.apache.bcel.internal.generic.NEW;
import com.tang.mall.common.utils.R;
import com.tang.mall.product.entity.SkuImagesEntity;
import com.tang.mall.product.entity.SpuInfoDescEntity;
import com.tang.mall.product.feign.SeckillFeignService;
import com.tang.mall.product.service.*;
import com.tang.mall.product.vo.SeckillSkuVo;
import com.tang.mall.product.vo.SkuItemSaleAttrVo;
import com.tang.mall.product.vo.SkuItemVo;
import com.tang.mall.product.vo.SpuItemAttrGroupVo;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tang.mall.common.utils.PageUtils;
import com.tang.mall.common.utils.Query;

import com.tang.mall.product.dao.SkuInfoDao;
import com.tang.mall.product.entity.SkuInfoEntity;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {
    @Resource
    SkuImagesService skuImagesService;

    @Resource
    SpuInfoDescService spuInfoDescService;

    @Resource
    AttrGroupService attrGroupService;

    @Resource
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Resource
    ThreadPoolExecutor executor;

    @Resource
    SeckillFeignService seckillFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SkuInfoEntity> wrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.and(w ->
                    w.eq("sku_id", key).or().like("sku_name", key)
            );
        }

        String catelogId = (String) params.get("catelogId");
        if (!StringUtils.isEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)) {
            wrapper.eq("catelog_id", catelogId);
        }

        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)) {
            wrapper.eq("brand_id", brandId);
        }

        String min = (String) params.get("min");
        if(!StringUtils.isEmpty(min)){
            wrapper.ge("price", min);
        }


        String max = (String) params.get("max");
        if(!StringUtils.isEmpty(max)){
            BigDecimal bigDecimalMax = new BigDecimal(max);
            if (bigDecimalMax.compareTo(new BigDecimal("0")) == 1) {
                wrapper.le("price", max);
            }
        }

        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuInfoEntity> getSkuBySpuId(Long spuId) {

        List<SkuInfoEntity> skuInfoEntities = this.list(new QueryWrapper<SkuInfoEntity>().eq("spu_id", spuId));
        return skuInfoEntities;
    }

    @Override
    public SkuItemVo itemInfo(Long skuId) throws ExecutionException, InterruptedException {
        SkuItemVo skuItemVo = new SkuItemVo();

        // sku基本信息
        CompletableFuture<SkuInfoEntity> infoFuture = CompletableFuture.supplyAsync(() -> {
            SkuInfoEntity info = getById(skuId);
            skuItemVo.setSkuInfoEntity(info);
            return info;
        }, executor);

        // sku销售属性组合
        CompletableFuture<Void> attrFuture = infoFuture.thenAcceptAsync((res) -> {
            List<SkuItemSaleAttrVo> saleAttrVos = skuSaleAttrValueService.getSaleAttrBySpuId(res.getSpuId());
            skuItemVo.setSpuSaleAttrs(saleAttrVos);
        }, executor);
        
        // spu的介绍
        CompletableFuture<Void> descFuture = infoFuture.thenAcceptAsync((res) -> {
            SpuInfoDescEntity spuInfo = spuInfoDescService.getById(res.getSpuId());
            skuItemVo.setSpuDesc(spuInfo);
        }, executor);

        // spu规格参数信息
        CompletableFuture<Void> baseFuture = infoFuture.thenAcceptAsync((res) -> {
            List<SpuItemAttrGroupVo> attrGroupVos = attrGroupService.getAttrGroupWithAttrsBySpuId(res.getSpuId(), res.getCatelogId());
            skuItemVo.setSpuAttrGroupVos(attrGroupVos);
        }, executor);

        // sku图片信息
        CompletableFuture<Void> imageFuture = CompletableFuture.runAsync(() -> {
            List<SkuImagesEntity> images = skuImagesService.getImagesBySkuId(skuId);
            skuItemVo.setSkuImages(images);
        }, executor);

        // 远程调用查询当前sku是否参与秒杀优惠活动
        CompletableFuture<Void> seckillFuture = CompletableFuture.runAsync(() -> {

            R skuSeckilInfo = seckillFeignService.getSkuSeckillInfo(skuId);
            if (skuSeckilInfo.get("code").equals(0)) {
                // 查询成功
                SeckillSkuVo seckilInfoData = skuSeckilInfo.getData("data", new TypeReference<SeckillSkuVo>() {});
                skuItemVo.setSeckillSkuVo(seckilInfoData);

                if (seckilInfoData != null) {
                    long currentTime = System.currentTimeMillis();
                    if (currentTime > seckilInfoData.getEndTime()) {
                        skuItemVo.setSeckillSkuVo(null);
                    }
                }
            }
        }, executor);

        // 等待全部任务完成
        CompletableFuture.allOf(attrFuture, baseFuture, descFuture, imageFuture, seckillFuture).get();
        
        return skuItemVo;
    }

}