package com.tang.mall.third.controller;

import com.tang.mall.common.utils.R;
import com.tang.mall.third.component.SmsComponent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author aidianfirst
 * @create 2021/11/16 11:06
 */
@RestController
@RequestMapping("/sms")
public class SmsSendController {
    @Resource
    SmsComponent smsComponent;

    // 提供功能给其他服务调用
    @GetMapping("/sendCode")
    public R sendCode(@RequestParam("phone") String phone, @RequestParam("code") String code){

        smsComponent.sendSmsCode(phone, code);
        return R.ok();
    }
}
