package com.tang.mall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tang.mall.common.utils.PageUtils;
import com.tang.mall.order.entity.RefundInfoEntity;

import java.util.Map;

/**
 * 退款信息
 *
 * @author aidianfirst
 * @email aidianfirst@gmail.com
 * @date 2021-10-29 15:29:23
 */
public interface RefundInfoService extends IService<RefundInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

