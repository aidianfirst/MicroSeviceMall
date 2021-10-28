package com.tang.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tang.common.utils.PageUtils;
import com.tang.mall.product.entity.SpuInfoEntity;

import java.util.Map;

/**
 * spu
 *
 * @author aidianfirst
 * @email aidianfirst@gmail.com
 * @date 2021-10-28 19:22:36
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);
}
