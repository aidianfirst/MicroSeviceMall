package com.tang.mall.order.dao;

import com.tang.mall.order.entity.OrderItemEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单项信息
 * 
 * @author aidianfirst
 * @email aidianfirst@gmail.com
 * @date 2021-10-29 15:29:23
 */
@Mapper
public interface OrderItemDao extends BaseMapper<OrderItemEntity> {
	
}
