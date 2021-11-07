package com.tang.mall.product.vo;

import com.tang.mall.product.entity.AttrEntity;
import com.tang.mall.product.entity.AttrGroupEntity;
import lombok.Data;

import java.util.List;

/**
 * @author aidianfirst
 * @create 2021/11/5 20:01
 */
@Data
public class AttrGroupWithAttrsVo extends AttrGroupEntity {
    private List<AttrEntity> attrs;
}
