package com.tang.mall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.tang.mall.common.utils.R;
import com.tang.mall.ware.feign.MemberFeignService;
import com.tang.mall.ware.vo.FareVo;
import com.tang.mall.ware.vo.MemberAddressVo;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tang.mall.common.utils.PageUtils;
import com.tang.mall.common.utils.Query;

import com.tang.mall.ware.dao.WareInfoDao;
import com.tang.mall.ware.entity.WareInfoEntity;
import com.tang.mall.ware.service.WareInfoService;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;


@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {
    @Resource
    MemberFeignService memberFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareInfoEntity> wrapper = new QueryWrapper<>();

        String key = (String) params.get("key");
        if(!StringUtils.isEmpty(key)){
            wrapper.eq("id", key)
                    .or().like("name", key)
                    .or().like("address", key)
                    .or().like("areacode", key);
        }
        IPage<WareInfoEntity> page = this.page(new Query<WareInfoEntity>().getPage(params),wrapper);

        return new PageUtils(page);
    }

    @Override
    public FareVo getFare(Long addrId) {
        FareVo fareVo = new FareVo();
        R info = memberFeignService.info(addrId);
        MemberAddressVo data = info.getData("memberReceiveAddress", new TypeReference<MemberAddressVo>() {});
        if(data != null){
            BigDecimal bigDecimal = new BigDecimal("10.0");
            fareVo.setFare(bigDecimal);
            fareVo.setAddress(data);
            return fareVo;
        }
        return null;
    }

}