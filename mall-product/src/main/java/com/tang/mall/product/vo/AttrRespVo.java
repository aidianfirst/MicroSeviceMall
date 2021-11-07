package com.tang.mall.product.vo;

import lombok.Data;

/**
 * @author aidianfirst
 * @create 2021/11/2 17:34
 */
@Data
public class AttrRespVo extends AttrVo {
    private String catelogName;

    private String groupName;

    private Long[] catelogPath;
}
