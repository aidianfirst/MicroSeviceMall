package com.tang.mall.cart.controller;

import com.tang.mall.cart.service.CartService;
import com.tang.mall.cart.vo.CartItemVo;
import com.tang.mall.cart.vo.CartVo;
import com.tang.mall.common.vo.MemberResponseVo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author aidianfirst
 * @create 2021/11/18 16:30
 */
@Controller
public class CartController {
    @Resource
    CartService cartService;

    @GetMapping("/currentUserCartItems")
    @ResponseBody
    public List<CartItemVo> getCurrentUserCartItems(){
        return cartService.getUserCartItems();
    }

    @GetMapping("/deleteItem")
    public String deleteItem(@RequestParam("skuId") Long skuId){
        cartService.deleteItem(skuId);
        return "redirect:http://cart.mall.com/cart.html";
    }

    @GetMapping("/countItem")
    public String countItem(@RequestParam("skuId") Long skuId, @RequestParam("num") Integer num){
        cartService.countItem(skuId, num);
        return "redirect:http://cart.mall.com/cart.html";
    }

    @GetMapping("/checkItem")
    public String checkItem(@RequestParam("skuId") Long skuId, @RequestParam("checked") Integer checked){
        cartService.checkItem(skuId, checked);
        return "redirect:http://cart.mall.com/cart.html";
    }

    @GetMapping("/cart.html")
    public String cartListPage(HttpSession session, Model model) throws ExecutionException, InterruptedException {
        MemberResponseVo loginUser = (MemberResponseVo)session.getAttribute("loginUser");
        if(loginUser == null){
            return "redirect:http://auth.mall.com/login.html";
        }else {
            CartVo cartVo = cartService.getCart();
            model.addAttribute("cart",cartVo);
            return "cartList";
        }
    }

    @GetMapping(value = "/addCartItem")
    public String addCartItem(@RequestParam("skuId") Long skuId,
                              @RequestParam("num") Integer num,
                              RedirectAttributes attributes) throws ExecutionException, InterruptedException {

        cartService.addToCart(skuId,num);

        attributes.addAttribute("skuId",skuId);

        return "redirect:http://cart.mall.com/addToCartSuccessPage.html";
    }

    @GetMapping(value = "/addToCartSuccessPage.html")
    public String addToCartSuccessPage(@RequestParam("skuId") Long skuId,
                                       Model model) {
        //重定向到成功页面。再次查询购物车数据即可
        CartItemVo cartItemVo = cartService.getCartItem(skuId);
        model.addAttribute("cartItem",cartItemVo);
        return "success";
    }

}
