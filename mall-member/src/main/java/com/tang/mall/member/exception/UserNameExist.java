package com.tang.mall.member.exception;

/**
 * @author aidianfirst
 * @create 2021/11/16 16:17
 */
public class UserNameExist extends RuntimeException{
    public UserNameExist() {
        super("用户名已存在");
    }
}
