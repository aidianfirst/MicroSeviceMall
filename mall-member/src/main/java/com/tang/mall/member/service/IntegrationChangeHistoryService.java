package com.tang.mall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tang.common.utils.PageUtils;
import com.tang.mall.member.entity.IntegrationChangeHistoryEntity;

import java.util.Map;

/**
 * 
 *
 * @author aidianfirst
 * @email aidianfirst@gmail.com
 * @date 2021-10-29 15:26:21
 */
public interface IntegrationChangeHistoryService extends IService<IntegrationChangeHistoryEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

