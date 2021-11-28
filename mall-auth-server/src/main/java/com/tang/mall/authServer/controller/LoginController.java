package com.tang.mall.authServer.controller;

import com.alibaba.fastjson.TypeReference;
import com.tang.mall.authServer.feign.MemberFeignService;
import com.tang.mall.authServer.vo.UserLoginVo;
import com.tang.mall.common.utils.R;
import com.tang.mall.common.vo.MemberResponseVo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * @author aidianfirst
 * @create 2021/11/15 22:29
 */
@Controller
public class LoginController {
    @Resource
    MemberFeignService memberFeignService;

    @PostMapping("/login")
    public String login(UserLoginVo vo, RedirectAttributes redirectAttributes, HttpSession session) {
        R login = memberFeignService.login(vo);
        if (login.get("code").equals(0)) {
            // 登录成功，设置session
            MemberResponseVo data = login.getData("data", new TypeReference<MemberResponseVo>() {});
            session.setAttribute("loginUser", data);
            return "redirect:http://mall.com";
        } else {
            Map<String, String> errors = new HashMap<>();
            errors.put("msg", login.getData("msg", new TypeReference<String>() {}));
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.mall.com/login.html";
        }
    }

    @GetMapping("/login.html")
    public String loginPage(HttpSession session){
        Object loginUser = session.getAttribute("loginUser");
        if (loginUser == null){
            return "login";
        }
        return "redirect:http://mall.com";
    }

    @GetMapping(value = "/logout.html")
    public String logout(HttpServletRequest request) {
        request.getSession().removeAttribute("loginUser");
        request.getSession().invalidate();
        return "redirect:http://mall.com";
    }
}
