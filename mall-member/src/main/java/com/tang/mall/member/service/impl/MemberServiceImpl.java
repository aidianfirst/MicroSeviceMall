package com.tang.mall.member.service.impl;

import com.tang.mall.member.exception.PhoneExist;
import com.tang.mall.member.exception.UserNameExist;
import com.tang.mall.member.vo.MemberLoginVo;
import com.tang.mall.member.vo.MemberRegistVo;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tang.mall.common.utils.PageUtils;
import com.tang.mall.common.utils.Query;

import com.tang.mall.member.dao.MemberDao;
import com.tang.mall.member.entity.MemberEntity;
import com.tang.mall.member.service.MemberService;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void regist(MemberRegistVo vo) {
        MemberEntity entity = new MemberEntity();
        MemberEntity levelEntity = baseMapper.getDefaultLevel();
        entity.setLevelId(levelEntity.getId());
        // spring 的 BCryptPasswordEncoder 进行加密
        BCryptPasswordEncoder bCrypt = new BCryptPasswordEncoder();
        String pwd = bCrypt.encode(vo.getPassword());
        entity.setPassword(pwd);
        // 需要校验唯一性，有问题抛出异常
        checkUserNameUnique(vo.getUsername());
        checkPhoneUnique(vo.getPhone());
        entity.setUsername(vo.getUsername());
        entity.setMobile(vo.getPhone());

        entity.setNickname(vo.getUsername());

        baseMapper.insert(entity);
    }

    @Override
    public MemberEntity login(MemberLoginVo vo) {
        String loginacct = vo.getLoginacct();
        String password = vo.getPassword();

        // 使用手机号或用户名登录
        MemberEntity entity = baseMapper.selectOne(
                new QueryWrapper<MemberEntity>().eq("username", loginacct).or().eq("mobile", loginacct));

        // 没有用户则登录失败，有用户则验证密码
        if(entity == null){
            return null;
        }else {
            String dbPassword = entity.getPassword();
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            // 密码匹配是否正确
            boolean matches = passwordEncoder.matches(password, dbPassword);
            if(matches){
                return entity;
            }else {
                return null;
            }
        }
    }

    private void checkUserNameUnique(String username) {
        Integer count = baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("username", username));
        if (count > 0) {
            throw new UserNameExist();
        }
    }

    private void checkPhoneUnique(String phone) {
        Integer count = baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", phone));
        if (count > 0) {
            throw new PhoneExist();
        }
    }

}