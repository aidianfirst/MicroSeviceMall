package com.tang.mall.common.to;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author aidianfirst
 * @create 2021/11/6 11:10
 */
@Data
public class SpuBoundTo {
    private Long spuId;
    private BigDecimal buyBounds;
    private BigDecimal growBrounds;
}
