package com.tang.mall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tang.mall.common.utils.PageUtils;
import com.tang.mall.member.entity.MemberReceiveAddressEntity;

import java.util.List;
import java.util.Map;

/**
 * 
 *
 * @author aidianfirst
 * @email aidianfirst@gmail.com
 * @date 2021-10-29 15:26:21
 */
public interface MemberReceiveAddressService extends IService<MemberReceiveAddressEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<MemberReceiveAddressEntity> getAddress(Long memberId);
}

