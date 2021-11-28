package com.tang.mall.common.exception;

/**
 * @author aidianfirst
 * @create 2021/11/1 0:01
 */
public enum CodeEnum {
    UNKOWN_EXCEPTION(10000, "系统未知异常"),
    VALID_EXCEPTION(10001, "参数格式校验失败"),
    PRODUCT_UP_EXCEPTION(10002,"商品上架异常"),
    SMS_CODE_EXCEPTION(10003,"短信验证码获取过快，请稍后再试"),
    USER_EXIST_EXCEPTION(10004,"用户名已存在"),
    PHONE_EXIST_EXCEPTION(10005,"手机号已存在"),
    LOGINACCT_PASSWORD_EXCEPTION(10006,"账号密码错误"),
    NO_STOCK_EXCEPTION(10007,"商品没有库存");

    private int code;
    private String msg;

    CodeEnum(int code, String msg){
        this.code = code;
        this.msg = msg;
    }

    public int getCode(){
        return code;
    }
    public String getMsg(){
        return msg;
    }
}
