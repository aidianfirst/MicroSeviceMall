package com.tang.mall.authServer.feign;

import com.tang.mall.authServer.vo.UserLoginVo;
import com.tang.mall.authServer.vo.UserRegistVo;
import com.tang.mall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author aidianfirst
 * @create 2021/11/16 16:07
 */
@FeignClient("mall-member")
public interface MemberFeignService {

    @PostMapping("/member/member/regist")
    R regist(@RequestBody UserRegistVo vo);

    @PostMapping("/member/member/login")
    R login(@RequestBody UserLoginVo vo);
}
