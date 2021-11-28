package com.tang.mall.common.exception;

/**
 * @author aidianfirst
 * @create 2021/11/21 16:53
 */
public class NoStockException extends RuntimeException{
    private Long skuId;

    public NoStockException(Long skuId){
        super("商品id：" + skuId + "的商品，没有足够库存");
    }

    public NoStockException(String msg) {
        super(msg);
    }

    public Long getSkuId(){
        return  skuId;
    }

    public void setSkuId(Long skuId){
        this.skuId = skuId;
    }
}
