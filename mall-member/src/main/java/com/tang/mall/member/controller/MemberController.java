package com.tang.mall.member.controller;

import java.util.Arrays;
import java.util.Map;

import com.tang.mall.common.exception.CodeEnum;
import com.tang.mall.member.exception.PhoneExist;
import com.tang.mall.member.exception.UserNameExist;
import com.tang.mall.member.vo.MemberLoginVo;
import com.tang.mall.member.vo.MemberRegistVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.tang.mall.member.entity.MemberEntity;
import com.tang.mall.member.service.MemberService;
import com.tang.mall.common.utils.PageUtils;
import com.tang.mall.common.utils.R;


/**
 * 
 *
 * @author aidianfirst
 * @email aidianfirst@gmail.com
 * @date 2021-10-29 15:26:21
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    @PostMapping("/login")
    public R login(@RequestBody MemberLoginVo vo){
        MemberEntity entity = memberService.login(vo);
        // 登录成功才会返回实体
        if(entity != null){
            return R.ok().setData(entity);
        }else {
            return R.error(CodeEnum.LOGINACCT_PASSWORD_EXCEPTION.getCode(), CodeEnum.LOGINACCT_PASSWORD_EXCEPTION.getMsg());
        }
    }

    @PostMapping("/regist")
    public R regist(@RequestBody MemberRegistVo vo){
        try {
            memberService.regist(vo);
            return R.ok();
        } catch (UserNameExist e) {
            return R.error(CodeEnum.USER_EXIST_EXCEPTION.getCode(), CodeEnum.USER_EXIST_EXCEPTION.getMsg());
        } catch (PhoneExist e) {
            return R.error(CodeEnum.PHONE_EXIST_EXCEPTION.getCode(), CodeEnum.PHONE_EXIST_EXCEPTION.getMsg());
        }
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
		MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody MemberEntity member){
		memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody MemberEntity member){
		memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
		memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
