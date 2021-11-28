package com.tang.mall.cart.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author aidianfirst
 * @create 2021/11/18 16:13
 */
@Data
public class CartVo {
    private List<CartItemVo> items; // 商品信息

    private Integer totalCount; // 总共数量

    private Integer countType; // 商品类型的数量

    private BigDecimal totalAccount; // 总价

    private BigDecimal reducePrice = new BigDecimal(0); // 优惠价

    public Integer getTotalCount() {
        int count = 0;
        if (items != null && items.size() > 0) {
            for (CartItemVo item : items) {
                count += item.getCount();
            }
        }
        return count;
    }

    public Integer getCountType() {
        int count = 0;
        if (items != null && items.size() > 0) {
            for (CartItemVo item : items) {
                count += 1;
            }
        }
        return count;
    }

    public BigDecimal getTotalAccount() {
        BigDecimal account = new BigDecimal(0);
        if (items != null && this.items.size() > 0) {
            // 1.计算总价
            for (CartItemVo item : items) {
                if (item.getCheck()) {
                    BigDecimal totalPrice = item.getTotalPrice();
                    account = account.add(totalPrice);
                }
            }
            // 2.减去优惠价格
            account = account.subtract(getReducePrice());
        }
        return account;
    }
}
