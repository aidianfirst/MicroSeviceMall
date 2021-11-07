package com.tang.mall.ware.vo;

import lombok.Data;

/**
 * @author aidianfirst
 * @create 2021/11/7 17:44
 */
@Data
public class PurchaseItemDoneVo {
    private Long itemId;
    private Integer status;
    private String reason;
}
