package com.litepan.annotation;

import org.springframework.web.bind.annotation.Mapping;

import java.lang.annotation.*;

@Target({ElementType.METHOD})       // 在方法上使用的注解
@Retention(RetentionPolicy.RUNTIME) // 保留策略：在运行时是可见的。可以在运行时通过反射来检查这个注解的存在和它的值
@Documented                         // 当使用Javadoc工具为Java代码生成文档时，该注解会被包含在生成的文档中。
@Mapping                            //
public @interface GlobalInterceptor {

    /**
     * 校验参数，默认不校验
     */
    boolean checkParams() default false;

}
