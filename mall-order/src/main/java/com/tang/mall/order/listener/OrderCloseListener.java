package com.tang.mall.order.listener;

import com.rabbitmq.client.Channel;
import com.tang.mall.common.to.mq.StockLockedTo;
import com.tang.mall.order.entity.OrderEntity;
import com.tang.mall.order.service.OrderService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * @author aidianfirst
 * @create 2021/11/23 10:30
 */
@Service
@RabbitListener(queues = "order.release.order.queue")
public class OrderCloseListener {
    @Resource
    OrderService orderService;

    @RabbitHandler
    public void listener(OrderEntity entity, Message message, Channel channel) throws IOException {
        System.out.println("订单过期，准备关单：" + entity.getOrderSn());

        try{
            orderService.closeOrder(entity);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }catch (Exception e){
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }
}
