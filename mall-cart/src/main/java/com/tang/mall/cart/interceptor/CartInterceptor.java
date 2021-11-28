package com.tang.mall.cart.interceptor;

import com.tang.mall.cart.to.UserInfoTo;
import com.tang.mall.common.vo.MemberResponseVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author aidianfirst
 * @create 2021/11/18 17:53
 */
@Slf4j
public class CartInterceptor implements HandlerInterceptor {
    public static ThreadLocal<UserInfoTo> ThreadLocal = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        UserInfoTo userInfoTo = new UserInfoTo();
        HttpSession session = request.getSession();
        //获得当前登录用户的信息
        MemberResponseVo memberResponseVo = (MemberResponseVo) session.getAttribute("loginUser");
        if (memberResponseVo != null) {
            userInfoTo.setUserId(memberResponseVo.getId());
        }
        ThreadLocal.set(userInfoTo);
        return true;
    }
}
