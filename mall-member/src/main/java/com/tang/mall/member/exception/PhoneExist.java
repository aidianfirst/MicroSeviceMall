package com.tang.mall.member.exception;

/**
 * @author aidianfirst
 * @create 2021/11/16 16:16
 */
public class PhoneExist extends RuntimeException{
    public PhoneExist() {
        super("手机号已存在");
    }
}
