package com.tang.mall.search.vo;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * @author aidianfirst
 * @create 2021/11/14 20:56
 */
@Data
public class AttrResponseVo {
    // 属性id
    @TableId
    private Long attrId;

    // 属性名
    private String attrName;

    // 是否需要检索[0-不需要，1-需要]
    private Integer searchType;

    // 属性图标
    private String icon;

    // 可选值列表[用逗号分隔]
    private String valueSelect;

    // 属性类型[0-销售属性，1-基本属性，2-既是销售属性又是基本属性]
    private Integer attrType;

    // 启用状态[0 - 禁用，1 - 启用]
    private Long enable;

    // 所属分类
    private Long catelogId;

    // 快速展示【是否展示在介绍上；0-否 1-是】，在sku中仍然可以调整
    private Integer showDesc;

    // 值类型[0-为单个值，1-可以选择多个值]
    private Long valueType;

    // 属性分组id
    private Long attrGroupId;

    // 属性分组分类名
    private String catelogName;

    // 属性分组名
    private String groupName;

    // 属性名称全路径
    private Long[] catelogPath;
}
