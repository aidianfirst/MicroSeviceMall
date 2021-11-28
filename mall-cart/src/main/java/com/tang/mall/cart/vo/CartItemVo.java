package com.tang.mall.cart.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author aidianfirst
 * @create 2021/11/18 16:13
 */
@Data
public class CartItemVo {
    private Long skuId;
    private Boolean check = true;
    private String title;
    private String image;
    private List<String> skuAttrValues;
    private BigDecimal price;
    private Integer count;
    private BigDecimal totalPrice;
    public BigDecimal getTotalPrice(){
        return this.price.multiply(new BigDecimal(this.count));
    }
}
