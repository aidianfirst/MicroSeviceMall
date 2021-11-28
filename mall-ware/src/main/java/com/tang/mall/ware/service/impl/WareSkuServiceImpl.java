package com.tang.mall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.rabbitmq.client.Channel;
import com.tang.mall.common.exception.NoStockException;
import com.tang.mall.common.to.mq.OrderTo;
import com.tang.mall.common.to.mq.StockDetailTo;
import com.tang.mall.common.to.mq.StockLockedTo;
import com.tang.mall.common.utils.R;
import com.tang.mall.ware.entity.WareOrderTaskDetailEntity;
import com.tang.mall.ware.entity.WareOrderTaskEntity;
import com.tang.mall.ware.feign.OrderFeignService;
import com.tang.mall.ware.feign.ProductFeignService;
import com.tang.mall.ware.service.WareOrderTaskDetailService;
import com.tang.mall.ware.service.WareOrderTaskService;
import com.tang.mall.ware.vo.OrderItemVo;
import com.tang.mall.ware.vo.OrderVo;
import com.tang.mall.ware.vo.WareSkuLockVo;
import lombok.Data;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tang.mall.common.utils.PageUtils;
import com.tang.mall.common.utils.Query;

import com.tang.mall.ware.dao.WareSkuDao;
import com.tang.mall.ware.entity.WareSkuEntity;
import com.tang.mall.ware.service.WareSkuService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {
    @Resource
    WareSkuDao wareSkuDao;

    @Resource
    ProductFeignService productFeignService;

    @Resource
    OrderFeignService orderFeignService;

    @Resource
    WareOrderTaskService wareOrderTaskService;

    @Resource
    WareOrderTaskDetailService wareOrderTaskDetailService;

    @Resource
    RabbitTemplate rabbitTemplate;



    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareSkuEntity> wrapper = new QueryWrapper<>();
        String skuId = (String) params.get("skuId");
        if(!StringUtils.isEmpty(skuId)){
            wrapper.eq("sku_id", skuId);
        }

        String wareId = (String) params.get("wareId");
        if(!StringUtils.isEmpty(wareId)){
            wrapper.eq("ware_id", wareId);
        }

        IPage<WareSkuEntity> page = this.page(new Query<WareSkuEntity>().getPage(params), wrapper);

        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        List<WareSkuEntity> wareSkuEntities = wareSkuDao.selectList(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
        if (!wareSkuEntities.isEmpty()) {
            // 如果不是空的,添加库存
            wareSkuDao.addStock(skuId, wareId, skuNum);
        } else {
            // 保存新的
            WareSkuEntity wareSkuEntity = new WareSkuEntity();
            wareSkuEntity.setSkuId(skuId);
            wareSkuEntity.setWareId(wareId);
            wareSkuEntity.setStock(skuNum);
            wareSkuEntity.setStockLocked(0);
            // 远程查询sku名字
            String skuName = productFeignService.getSkuName(skuId);
            wareSkuEntity.setSkuName(skuName);
            wareSkuDao.insert(wareSkuEntity);
        }
    }

    // 订单锁库存
    @Transactional
    @Override
    public Boolean orderLockStock(WareSkuLockVo vo) {

        // 保存库存工作单详情信息
        WareOrderTaskEntity wareOrderTaskEntity = new WareOrderTaskEntity();
        wareOrderTaskEntity.setOrderSn(vo.getOrderSn());
        wareOrderTaskEntity.setCreateTime(new Date());
        wareOrderTaskService.save(wareOrderTaskEntity);

        // 查找商品在哪个仓库有库存
        List<OrderItemVo> locks = vo.getLocks();

        List<SkuWareHasStock> collect = locks.stream().map(item -> {
            SkuWareHasStock stock = new SkuWareHasStock();
            Long skuId = item.getSkuId();
            stock.setSkuId(skuId);
            stock.setNum(item.getCount());

            //查询这个商品在哪个仓库有库存，会查出多个符合条件的仓库
            List<Long> wareIdList = wareSkuDao.listWareIdHasSkuStock(skuId);
            stock.setWareId(wareIdList);
            return stock;
        }).collect(Collectors.toList());

        //2、逐个商品进行锁定库存
        for (SkuWareHasStock hasStock : collect) {
            boolean skuStocked = false;
            Long skuId = hasStock.getSkuId();
            List<Long> wareIds = hasStock.getWareId();

            if (StringUtils.isEmpty(wareIds)) {
                //没有任何仓库有这个商品的库存
                throw new NoStockException(skuId);
            }

            // 有多个仓库，逐个判定是否能锁定库存
            //1、如果每一个商品都锁定成功,将当前商品锁定了几件的工作单记录发给MQ
            //2、锁定失败。前面保存的工作单信息都回滚了。发送出去的消息，即使要解锁库存，由于在数据库查不到指定的id，所有就不用解锁
            for (Long wareId : wareIds) {
                //锁定成功就返回1，失败就返回0
                Long count = wareSkuDao.lockSkuStock(skuId, wareId, hasStock.getNum());

                if (count == 1) {
                    // 锁住，标识位记为true，并告诉mq
                    skuStocked = true;

                    WareOrderTaskDetailEntity taskDetailEntity = WareOrderTaskDetailEntity.builder()
                            .skuId(skuId)
                            .skuName("")
                            .skuNum(hasStock.getNum())
                            .taskId(wareOrderTaskEntity.getId())
                            .wareId(wareId)
                            .lockStatus(1)
                            .build();
                    wareOrderTaskDetailService.save(taskDetailEntity);

                    // mq路由信息
                    StockLockedTo lockedTo = new StockLockedTo();
                    lockedTo.setId(wareOrderTaskEntity.getId());
                    StockDetailTo detailTo = new StockDetailTo();
                    BeanUtils.copyProperties(taskDetailEntity,detailTo);
                    lockedTo.setDetailTo(detailTo);
                    rabbitTemplate.convertAndSend("stock-event-exchange", "stock.locked", lockedTo);

                    break;
                }
            }

            // 当前商品没有一个仓库可以锁定库存
            if (skuStocked == false) {
                throw new NoStockException(skuId);
            }
        }
        return true;
    }

    @Override
    public void unLockStock(StockLockedTo to) {
        //库存工作单的id
        StockDetailTo detail = to.getDetailTo();
        Long detailId = detail.getId();
        /**
         * 解锁，先查询数据库关于这个订单锁定库存信息
         * 有数据证明库存锁定成功了，根据订单状况解锁
         * 1、没有这个订单，说明订单出现问题回滚，必须解锁库存
         * 2、有这个订单，根据订单状态解锁库存
         *    订单已取消：解锁库存
         *    订单已支付：不解锁库存
         */
        WareOrderTaskDetailEntity taskDetailEntity = wareOrderTaskDetailService.getById(detailId);
        if(taskDetailEntity != null){
            Long id = to.getId();
            WareOrderTaskEntity taskEntity = wareOrderTaskService.getById(id);
            String orderSn = taskEntity.getOrderSn();
            R orderStatus = orderFeignService.getOrderStatus(orderSn);
            // 查询订单状态
            if(orderStatus.get("code").equals(0)){
                OrderVo data = orderStatus.getData("data", new TypeReference<OrderVo>() {});
                // 订单不存在或者被取消，解库存
                if(data == null || data.getStatus() == 4){
                    // 只有处于锁定的订单才需要解锁
                    if (taskDetailEntity.getLockStatus() == 1) {
                        unLock(detail.getSkuId(), detail.getWareId(), detail.getSkuNum(), detailId);
                    }
                }
            }else {
                //消息拒绝以后重新放在队列里面，继续消费解锁
                //远程调用服务失败
                throw new RuntimeException("远程调用服务失败");
            }
        }else {
            // 库存订单为空，无需解锁
        }
    }

    // 解库存在订单操作之前执行的情况
    // 多个订单解锁，事务处理，有一个解锁失败就回滚
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void unLockStock(OrderTo orderTo) {
        String orderSn = orderTo.getOrderSn();
        // 核对最新的库存操作，防止重复解库存
        WareOrderTaskEntity entity = wareOrderTaskService.getOrderTaskByOrderSn(orderSn);

        // 根据工作单id，查找所有未解锁的库存进行解锁
        Long id = entity.getId();
        List<WareOrderTaskDetailEntity> list = wareOrderTaskDetailService.list(
                new QueryWrapper<WareOrderTaskDetailEntity>()
                .eq("task_id", id)
                .eq("lock_status", 1)
        );

        for (WareOrderTaskDetailEntity taskDetailEntity : list) {
            unLock(taskDetailEntity.getSkuId(),
                    taskDetailEntity.getWareId(),
                    taskDetailEntity.getSkuNum(),
                    taskDetailEntity.getId());
        }

    }

    public void unLock(Long skuId,Long wareId,Integer num,Long taskDetailId) {
        // 解锁库存
        wareSkuDao.unLockStock(skuId, wareId, num);
        //更新库存工作单的状态
        WareOrderTaskDetailEntity taskDetailEntity = new WareOrderTaskDetailEntity();
        taskDetailEntity.setId(taskDetailId);
        //变为已解锁
        taskDetailEntity.setLockStatus(2);
        wareOrderTaskDetailService.updateById(taskDetailEntity);
    }

    @Data
    class SkuWareHasStock {
        private Long skuId;
        private Integer num;
        private List<Long> wareId;
    }
}