package com.litepan.annotation;

import com.litepan.enums.VerifyRegexEnum;

import java.lang.annotation.*;

@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface VerifyParam {

    int min() default -1;

    int max() default -1;

    /**
     * 参数是否为必须的，默认为否
     */
    boolean required() default false;

    /**
     * 参数校验规则，默认不校验
     */
    VerifyRegexEnum regex() default VerifyRegexEnum.NO;
}
