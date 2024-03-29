package com.tang.mall.common.to.mq;

import lombok.Data;

/**
 * @author aidianfirst
 * @create 2021/11/22 15:42
 */
@Data
public class StockLockedTo {
    /** 库存工作单的id **/
    private Long id;

    /** 工作单详情的所有信息 **/
    private StockDetailTo detailTo;

}
