package com.tang.mall.authServer.controller;

import com.alibaba.fastjson.TypeReference;
import com.tang.mall.authServer.feign.MemberFeignService;
import com.tang.mall.authServer.feign.ThirdFeignService;
import com.tang.mall.authServer.vo.UserRegistVo;
import com.tang.mall.common.exception.CodeEnum;
import com.tang.mall.common.utils.R;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author aidianfirst
 * @create 2021/11/16 11:58
 */
@Controller
public class RegistController {
    @Resource
    ThirdFeignService thirdFeignService;

    @Resource
    MemberFeignService memberFeignService;

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @ResponseBody
    @GetMapping("/sms/sendCode")
    public R sendCode(@RequestParam("phone") String phone) {
        // 先获取当前手机的验证码信息
        String redisCode = stringRedisTemplate.opsForValue().get("sms:" + phone);
        // 验证码信息不为空则继续判断，若其生成时间和当前时间间隔不到60s，则需要等待，这是为了让前端逻辑更严谨
        if (!StringUtils.isEmpty(redisCode)) {
            long time = Long.parseLong(redisCode.split("_")[1]);
            if (System.currentTimeMillis() - time < 60 * 1000) {
                return R.error(CodeEnum.SMS_CODE_EXCEPTION.getCode(), CodeEnum.SMS_CODE_EXCEPTION.getMsg());
            }
        }

        // 防止验证码多次校验 redis存储kv，redis缓存后，防止同一个手机在60s内重复发送验证码
        // 生成随机验证码时拼接系统时间，方便后续校验
        String code = UUID.randomUUID().toString().substring(0, 5) + "_" + System.currentTimeMillis();
        stringRedisTemplate.opsForValue().set("sms:" + phone, code, 5, TimeUnit.MINUTES);

        thirdFeignService.sendCode(phone, code.substring(0, 5));
        return R.ok();
    }

    // UserRegistVo使用了校验注解，其按照实体类条件校验，BindingResult是对应注解的结果返回类
    @PostMapping("/register")
    public String regist(@Valid UserRegistVo vo, BindingResult result, RedirectAttributes redirectAttributes) {
        // 校验出错，回到注册页
        if (result.hasErrors()) {
            Map<String, String> errors = result.getFieldErrors().stream().collect(
                    Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage)
            );
            redirectAttributes.addFlashAttribute("errors", errors);

            return "redirect:http://auth.mall.com/regist.html";
        }

        // 校验验证码
        String code = vo.getCode();
        String cache = stringRedisTemplate.opsForValue().get("sms:" + vo.getPhone());
        if (!StringUtils.isEmpty(cache)) {
            if (code.equals(cache.substring(0, 5))) {
                // 校验通过，先删除验证码，令牌机制
                stringRedisTemplate.delete("sms:" + vo.getPhone());
                // 通过验证进行注册
                R regist = memberFeignService.regist(vo);
                if(regist.get("code").equals(0)){
                    return "redirect:http://auth.mall.com/login.html";
                }else {
                    Map<String, String> errors = new HashMap<>();
                    errors.put("msg", regist.getData("msg", new TypeReference<String>() {}));
                    redirectAttributes.addFlashAttribute("errors", errors);
                    return "redirect:http://auth.mall.com/regist.html";
                }
            }else {
                Map<String, String> errors = new HashMap<>();
                errors.put("code", "验证码不存在");
                redirectAttributes.addFlashAttribute("errors", errors);
                return "redirect:http://auth.mall.com/regist.html";
            }
        }else {
            Map<String, String> errors = new HashMap<>();
            errors.put("code", "验证码不存在");
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.mall.com/regist.html";
        }
    }
}

