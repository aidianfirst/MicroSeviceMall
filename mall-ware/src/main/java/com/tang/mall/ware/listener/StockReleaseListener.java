package com.tang.mall.ware.listener;

import com.rabbitmq.client.Channel;
import com.tang.mall.common.to.mq.OrderTo;
import com.tang.mall.common.to.mq.StockLockedTo;
import com.tang.mall.ware.service.WareSkuService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * @author aidianfirst
 * @create 2021/11/23 0:09
 */
@Service
@RabbitListener(queues = "stock.release.stock.queue")
public class StockReleaseListener {
    @Resource
    WareSkuService wareSkuService;

    @RabbitHandler
    public void lockedListener(StockLockedTo to, Message message, Channel channel) throws IOException {
        System.out.println("-------------解库存-------------");
        // 解锁方法抛出异常，即代表消息消费失败，进行捕获异常并处理mq
        try {
            //解锁库存
            wareSkuService.unLockStock(to);
            // 手动删除消息
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (Exception e) {
            // 解锁失败 将消息重新放回队列，让别人消费
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }

    @RabbitHandler
    public void closedListener(OrderTo orderTo, Message message, Channel channel) throws IOException {
        System.out.println("-------------订单关闭，解库存-------------");
        // 解锁方法抛出异常，即代表消息消费失败，进行捕获异常并处理mq
        try {
            //解锁库存
            wareSkuService.unLockStock(orderTo);
            // 手动删除消息
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (Exception e) {
            // 解锁失败 将消息重新放回队列，让别人消费
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }

}
