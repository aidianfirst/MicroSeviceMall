package com.tang.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tang.mall.common.utils.PageUtils;
import com.tang.mall.product.entity.AttrEntity;
import com.tang.mall.product.vo.AttrGroupRelationVo;
import com.tang.mall.product.vo.AttrRespVo;
import com.tang.mall.product.vo.AttrVo;

import java.util.List;
import java.util.Map;

/**
 * 
 *
 * @author aidianfirst
 * @email aidianfirst@gmail.com
 * @date 2021-10-28 19:22:36
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveAttr(AttrVo attr);

    PageUtils queryBaseAttrPage(Map<String, Object> params, Long catelogId, String attrType);

    AttrRespVo getAttrinfo(Long attrId);

    void updateAttr(AttrVo attr);

    List<AttrEntity> getRelationAttr(Long attrgroupId);

    void deleRelation(AttrGroupRelationVo[] vos);

    PageUtils getNoRelationAttr(Map<String, Object> params, Long attrgroupId);

    // 在属性字段中，找到可用于检索的属性
    List<Long> selectSearchAttrs(List<Long> attrIds);
}

