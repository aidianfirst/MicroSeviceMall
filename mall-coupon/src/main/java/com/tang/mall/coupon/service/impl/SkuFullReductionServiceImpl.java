package com.tang.mall.coupon.service.impl;

import com.tang.common.to.MemberPrice;
import com.tang.common.to.SkuReductionTo;
import com.tang.mall.coupon.entity.MemberPriceEntity;
import com.tang.mall.coupon.entity.SkuLadderEntity;
import com.tang.mall.coupon.service.MemberPriceService;
import com.tang.mall.coupon.service.SkuLadderService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tang.common.utils.PageUtils;
import com.tang.common.utils.Query;

import com.tang.mall.coupon.dao.SkuFullReductionDao;
import com.tang.mall.coupon.entity.SkuFullReductionEntity;
import com.tang.mall.coupon.service.SkuFullReductionService;

import javax.annotation.Resource;


@Service("skuFullReductionService")
public class SkuFullReductionServiceImpl extends ServiceImpl<SkuFullReductionDao, SkuFullReductionEntity> implements SkuFullReductionService {
    @Resource
    SkuLadderService skuLadderService;

    @Resource
    MemberPriceService memberPriceService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuFullReductionEntity> page = this.page(
                new Query<SkuFullReductionEntity>().getPage(params),
                new QueryWrapper<SkuFullReductionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuReduction(SkuReductionTo skuReductionTo) {
        // 满减价格
        SkuLadderEntity ladderEntity = new SkuLadderEntity();
        ladderEntity.setSkuId(skuReductionTo.getSkuId());
        ladderEntity.setFullCount(skuReductionTo.getFullCount());
        ladderEntity.setDiscount(skuReductionTo.getDiscount());
        ladderEntity.setAddOther(skuReductionTo.getCountStatus());
        // 过滤空数据
        if(skuReductionTo.getFullCount() > 0){
            skuLadderService.save(ladderEntity);
        }

        // 满减信息
        SkuFullReductionEntity fullReductionEntity = new SkuFullReductionEntity();
        BeanUtils.copyProperties(skuReductionTo, fullReductionEntity);
        if(fullReductionEntity.getFullPrice().compareTo(new BigDecimal("0")) == 1){
            this.save(fullReductionEntity);
        }

        // 会员价
        List<MemberPrice> memberPrice = skuReductionTo.getMemberPrice();
        List<MemberPriceEntity> memberPriceEntityList = memberPrice.stream().map(item -> {
            MemberPriceEntity priceEntity = new MemberPriceEntity();
            priceEntity.setAddOther(skuReductionTo.getCountStatus());
            priceEntity.setMemberLevelId(item.getId());
            priceEntity.setMemberLevelName(item.getName());
            priceEntity.setMemberPrice(item.getPrice());
            priceEntity.setSkuId(skuReductionTo.getSkuId());
            return priceEntity;
        }).filter(item ->
                item.getMemberPrice().compareTo(new BigDecimal("0")) == 1
        ).collect(Collectors.toList());
        memberPriceService.saveBatch(memberPriceEntityList);

    }

}