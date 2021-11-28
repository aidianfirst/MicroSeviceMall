package com.tang.mall.third;

/**
 * @author aidianfirst
 * @create 2021/11/16 10:18
 */

import com.tang.mall.third.component.SmsComponent;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MallThirdApplication.class)
public class MallThirdApplicationTests {
    @Resource
    SmsComponent smsComponent;

    @Test
    public void sendMsg(){
        smsComponent.sendSmsCode("13397178339", "8888");
    }
}
