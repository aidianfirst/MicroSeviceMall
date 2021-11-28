package com.tang.mall.ware.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author aidianfirst
 * @create 2021/11/20 23:33
 */
@Data
public class FareVo {
    private MemberAddressVo address;
    private BigDecimal fare;
}

