package com.tang.mall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tang.mall.common.utils.PageUtils;
import com.tang.mall.coupon.entity.SeckillPromotionEntity;

import java.util.Map;

/**
 * 
 *
 * @author aidianfirst
 * @email aidianfirst@gmail.com
 * @date 2021-10-29 15:00:05
 */
public interface SeckillPromotionService extends IService<SeckillPromotionEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

