package com.tang.mall.product.exception;

import com.tang.common.exception.CodeEnum;
import com.tang.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.apache.bcel.classfile.Code;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 集中处理异常
 * @author aidianfirst
 * @create 2021/10/31 22:31
 */
@Slf4j
@RestControllerAdvice(basePackages = "com.tang.mall.product.controller")
public class MallExceptionControllerAdvice {
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R handleValidException(MethodArgumentNotValidException e){
        log.error("数据校验异常:{}，异常类型是{}", e.getMessage(), e.getClass());
        BindingResult bindingResult = e.getBindingResult();
        Map<String, String> errorMap = new HashMap<>();
        bindingResult.getFieldErrors().forEach(fieldError ->
            errorMap.put(fieldError.getField(), fieldError.getDefaultMessage())
        );
        return R.error(CodeEnum.VALID_EXCEPTION.getCode(), CodeEnum.VALID_EXCEPTION.getMsg()).put("data", errorMap);
    }

    @ExceptionHandler(value = Throwable.class)
    public R handleException(Throwable throwable){
        return R.error(CodeEnum.UNKOWN_EXCEPTION.getCode(), CodeEnum.UNKOWN_EXCEPTION.getMsg());
    }
}
