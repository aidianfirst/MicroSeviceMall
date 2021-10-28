package com.tang.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tang.common.utils.PageUtils;
import com.tang.mall.product.entity.SkuInfoEntity;

import java.util.Map;

/**
 * sku
 *
 * @author aidianfirst
 * @email aidianfirst@gmail.com
 * @date 2021-10-28 19:22:36
 */
public interface SkuInfoService extends IService<SkuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

