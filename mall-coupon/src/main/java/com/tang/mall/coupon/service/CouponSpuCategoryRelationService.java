package com.tang.mall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tang.common.utils.PageUtils;
import com.tang.mall.coupon.entity.CouponSpuCategoryRelationEntity;

import java.util.Map;

/**
 * 
 *
 * @author aidianfirst
 * @email aidianfirst@gmail.com
 * @date 2021-10-29 15:00:05
 */
public interface CouponSpuCategoryRelationService extends IService<CouponSpuCategoryRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);
}
