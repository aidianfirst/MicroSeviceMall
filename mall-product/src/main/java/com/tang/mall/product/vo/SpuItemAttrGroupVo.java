package com.tang.mall.product.vo;

import lombok.Data;

import java.util.List;

/**
 * @author aidianfirst
 * @create 2021/11/15 15:15
 */
@Data
public class SpuItemAttrGroupVo {
    private String groupName;
    private List<Attr> spuBaseAttrVos;
}

