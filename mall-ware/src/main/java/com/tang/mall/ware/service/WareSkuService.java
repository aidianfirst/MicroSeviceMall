package com.tang.mall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tang.mall.common.to.mq.OrderTo;
import com.tang.mall.common.to.mq.StockLockedTo;
import com.tang.mall.common.utils.PageUtils;
import com.tang.mall.ware.entity.WareSkuEntity;
import com.tang.mall.ware.vo.LockStockResultVo;
import com.tang.mall.ware.vo.SkuHasStockVo;
import com.tang.mall.ware.vo.WareSkuLockVo;

import java.util.List;
import java.util.Map;

/**
 * 
 *
 * @author aidianfirst
 * @email aidianfirst@gmail.com
 * @date 2021-10-29 15:32:25
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addStock(Long skuId, Long wareId, Integer skuNum);

    Boolean orderLockStock(WareSkuLockVo vo);

    void unLockStock(StockLockedTo to);

    void unLockStock(OrderTo orderTo);
}

