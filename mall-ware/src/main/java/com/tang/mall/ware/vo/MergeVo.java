package com.tang.mall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * @author aidianfirst
 * @create 2021/11/7 16:29
 */
@Data
public class MergeVo {
    private Long purchaseId;
    private List<Long> items;
}
