package com.tang.mall.order.web;

import com.alipay.api.AlipayApiException;
import com.tang.mall.order.config.AlipayTemplate;
import com.tang.mall.order.service.OrderService;
import com.tang.mall.order.vo.PayVo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

/**
 * @author aidianfirst
 * @create 2021/11/23 21:55
 */
@Controller
public class PayWebController {
    @Resource
    AlipayTemplate alipayTemplate;

    @Resource
    OrderService orderService;

    @ResponseBody
    @GetMapping(value = "/aliPayOrder", produces = "text/html")
    public String payOrder(@RequestParam("orderSn") String orderSn) throws AlipayApiException {
        PayVo payVo = orderService.getOrderPay(orderSn);
        String pay = alipayTemplate.pay(payVo);

        System.out.println(pay);
        return pay;
    }
}

