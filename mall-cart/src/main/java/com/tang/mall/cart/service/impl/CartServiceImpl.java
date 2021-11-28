package com.tang.mall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.tang.mall.cart.feign.ProductFeignService;
import com.tang.mall.cart.interceptor.CartInterceptor;
import com.tang.mall.cart.service.CartService;
import com.tang.mall.cart.to.UserInfoTo;
import com.tang.mall.cart.vo.CartItemVo;
import com.tang.mall.cart.vo.CartVo;
import com.tang.mall.cart.vo.SkuInfoVo;
import com.tang.mall.common.utils.R;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @author aidianfirst
 * @create 2021/11/18 17:36
 */
@Service("cartService")
public class CartServiceImpl implements CartService {
    private static final String CART_PREFIX = "mall:cart:";
    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Resource
    ProductFeignService productFeignService;

    @Resource
    ThreadPoolExecutor threadPoolExecutor;

    @Override
    public CartItemVo addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException {
        // 获取用户id
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        //判断Redis是否有该商品的信息
        String productRedisValue = (String) cartOps.get(skuId.toString());

        //如果没有就添加数据
        if (StringUtils.isEmpty(productRedisValue)) {
            CartItemVo cartItem = new CartItemVo();

            // 1、远程调用查询商品信息，执行异步任务查询并封装，提高业务效率
            CompletableFuture<Void> getSkuInfoTask = CompletableFuture.runAsync(() -> {
                R skuInfo = productFeignService.info(skuId);
                SkuInfoVo data = skuInfo.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                });
                cartItem.setCheck(true);
                cartItem.setCount(num);
                cartItem.setImage(data.getSkuDefaultImg());
                cartItem.setTitle(data.getSkuTitle());
                cartItem.setSkuId(skuId);
                cartItem.setPrice(data.getPrice());
            }, threadPoolExecutor);
            // 2、调用远端服务，封装组合信息
            CompletableFuture<Void> getSkuSaleAttrValues = CompletableFuture.runAsync(() -> {
                List<String> values = productFeignService.getSkuSaleAttrValues(skuId);
                cartItem.setSkuAttrValues(values);
            }, threadPoolExecutor);

            CompletableFuture.allOf(getSkuInfoTask, getSkuSaleAttrValues).get();

            String cartItemJson = JSON.toJSONString(cartItem);
            cartOps.put(skuId.toString(), cartItemJson);

            return cartItem;
        } else {
            //购物车有此商品，修改数量即可
            CartItemVo cartItem = JSON.parseObject(productRedisValue, CartItemVo.class);
            cartItem.setCount(cartItem.getCount() + num);
            //修改redis的数据
            String cartItemJson = JSON.toJSONString(cartItem);
            cartOps.put(skuId.toString(), cartItemJson);

            return cartItem;
        }
    }


    @Override
    public CartItemVo getCartItem(Long skuId) {
        //拿到要操作的购物车信息
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String redisValue = (String) cartOps.get(skuId.toString());
        CartItemVo cartItemVo = JSON.parseObject(redisValue, CartItemVo.class);

        return cartItemVo;
    }

    @Override
    public CartVo getCart() {
        CartVo cartVo = new CartVo();
        UserInfoTo userInfoTo = CartInterceptor.ThreadLocal.get();
        if (userInfoTo.getUserId() != null) {
            String cartKey = CART_PREFIX + userInfoTo.getUserId();
            List<CartItemVo> cartItems = getCartItems(cartKey);
            cartVo.setItems(cartItems);
        }
        return cartVo;
    }

    // 购物车中商品选中状态
    @Override
    public void checkItem(Long skuId, Integer checked) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        CartItemVo cartItem = getCartItem(skuId);
        cartItem.setCheck(checked == 1);
        String cartItemJson = JSON.toJSONString(cartItem);
        cartOps.put(skuId.toString(), cartItemJson);
    }

    // 购物车修改数量
    @Override
    public void countItem(Long skuId, Integer num) {
        CartItemVo cartItem = getCartItem(skuId);
        cartItem.setCount(num);

        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.put(skuId.toString(), JSON.toJSONString(cartItem));
    }

    @Override
    public void deleteItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.delete(skuId.toString());
    }

    @Override
    public List<CartItemVo> getUserCartItems() {
        UserInfoTo userInfoTo = CartInterceptor.ThreadLocal.get();
        if(userInfoTo.getUserId() != null){
            String cartKey = CART_PREFIX + userInfoTo.getUserId();
            List<CartItemVo> cartItems = getCartItems(cartKey);
            // 获取选中的购物项，更新最新的价格，而不是购物车的旧价
            List<CartItemVo> collect = cartItems.stream()
                    .filter(item -> item.getCheck())
                    .map(item -> {
                        item.setPrice(productFeignService.getNewPrice(item.getSkuId()));
                        return item;
                    })
                    .collect(Collectors.toList());
            return collect;
        }else{
            return null;
        }

    }

    // 通过拦截器获取session信息，查看当前购物车信息
    private BoundHashOperations<String, Object, Object> getCartOps() {
        //先得到当前用户信息
        UserInfoTo userInfoTo = CartInterceptor.ThreadLocal.get();
        String cartKey = "";
        if (userInfoTo.getUserId() != null) {
            cartKey = CART_PREFIX + userInfoTo.getUserId();
        }
        //绑定指定的key操作Redis
        BoundHashOperations<String, Object, Object> operations = stringRedisTemplate.boundHashOps(cartKey);

        return operations;
    }

    private List<CartItemVo> getCartItems(String cartKey) {
        // 获取购物车里面的所有商品
        BoundHashOperations<String, Object, Object> operations = stringRedisTemplate.boundHashOps(cartKey);
        List<Object> values = operations.values();
        if (values != null && values.size() > 0) {
            List<CartItemVo> cartItemVoStream = values.stream().map((obj) -> {
                String str = (String) obj;
                CartItemVo cartItem = JSON.parseObject(str, CartItemVo.class);
                return cartItem;
            }).collect(Collectors.toList());
            return cartItemVoStream;
        }
        return null;

    }
}
