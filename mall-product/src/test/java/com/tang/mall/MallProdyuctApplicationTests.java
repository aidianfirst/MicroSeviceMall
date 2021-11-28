package com.tang.mall;

import com.tang.mall.product.MallProductApplication;
import com.tang.mall.product.dao.AttrGroupDao;
import com.tang.mall.product.service.CategoryService;
import com.tang.mall.product.vo.SpuItemAttrGroupVo;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * @author aidianfirst
 * @create 2021/11/2 12:24
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MallProductApplication.class)
public class MallProdyuctApplicationTests {
    @Resource
    CategoryService  categoryService;

    @Resource
    RedissonClient redissonClient;

    @Resource
    AttrGroupDao attrGroupDao;

    @Test
    public void testPath(){
        Long[] catelogPath = categoryService.findCatelogPath(225L);
        log.info("路径:{}", Arrays.asList(catelogPath));
    }

    @Test
    public void redisson(){
        System.out.println(redissonClient);
    }

    @Test
    public void testgroup(){
        List<SpuItemAttrGroupVo> attrGroupWithAttrsBySpuId = attrGroupDao.getAttrGroupWithAttrsBySpuId(24L, 225L);
        System.out.println(attrGroupWithAttrsBySpuId);
    }

}
