package com.tang.mall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tang.mall.common.to.mq.SeckillOrderTo;
import com.tang.mall.common.utils.PageUtils;
import com.tang.mall.order.entity.OrderEntity;
import com.tang.mall.order.vo.OrderConfirmVo;
import com.tang.mall.order.vo.OrderSubmitVo;
import com.tang.mall.order.vo.PayVo;
import com.tang.mall.order.vo.SubmitOrderResponseVo;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * 订单
 *
 * @author aidianfirst
 * @email aidianfirst@gmail.com
 * @date 2021-10-29 15:29:23
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException;

    SubmitOrderResponseVo submitOrder(OrderSubmitVo vo);

    OrderEntity getOrderByOrderSn(String orderSn);

    void closeOrder(OrderEntity entity);

    PayVo getOrderPay(String orderSn);

    void createSeckillOrder(SeckillOrderTo orderTo);
}

