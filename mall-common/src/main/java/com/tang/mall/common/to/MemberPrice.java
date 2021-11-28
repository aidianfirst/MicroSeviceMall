/**
  * Copyright 2021 json.cn 
  */
package com.tang.mall.common.to;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MemberPrice {

    private Long id;
    private String name;
    private BigDecimal price;
}