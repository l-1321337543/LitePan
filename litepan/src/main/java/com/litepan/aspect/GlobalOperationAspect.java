package com.litepan.aspect;

import com.litepan.annotation.GlobalInterceptor;
import com.litepan.annotation.VerifyParam;
import com.litepan.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * @author 李臣洋
 * @version 1.0
 * @description: 全局切面
 * @date 2024/7/21 16:21
 */
@Aspect
@Component
@Slf4j
public class GlobalOperationAspect {

    /**
     * 此处定义一个可复用的切点表达式，即被@GlobalInterceptor注解标注的位置 <br>
     * 该切点表达式可以被其他通知直接引用，例如 @Before("requestInterceptor()") ，在通知使用的注解里面引用此方法
     */
    @Pointcut("@annotation(com.litepan.annotation.GlobalInterceptor)")
    private void requestInterceptor() {
    }

    /**
     * 定义一个Before通知，引用了requestInterceptor()方法的切点表达式，会在标注@GlobalInterceptor注解的方法执行前执行该方法
     *
     * @param point
     */
    @Before("requestInterceptor()")
    public void interceptorDo(JoinPoint point) {
        try {

            Object[] args = point.getArgs();
            // 此处有两种方式获取Method对象

            // 方式一:通过MethodSignature获取方法
            // 这种方式的优点是它直接关联到被拦截的方法，不需要额外的查找或反射。
            // 缺点是，它可能不适用于某些场景，特别是当被拦截的方法是在运行时通过代理或动态代理创建的（例如使用CGLIB或JDK动态代理）
            // 因为Method对象可能只适用于原始类，而不是代理类。
            // MethodSignature methodSignature = (MethodSignature) point.getSignature();
            // Method method = methodSignature.getMethod();

            // 方式二:通过反射获取方法
            // 这种方式通过目标对象（target）的类（target.getClass()）和方法名（methodName）以及参数类型（parameterTypes）来查找方法。
            // 它使用了Java的反射API，通过Class.getMethod()方法来获取Method对象。
            //这种方式的优点是它更加通用，因为它基于Java的反射机制，可以应用于任何类和方法，包括代理类。
            // 然而，它的缺点是性能较差，因为反射通常比直接访问方法要慢得多。
            // 此外，如果目标类有多个重载方法，你需要确保传递正确的参数类型数组来避免NoSuchMethodException。
            Object target = point.getTarget();
            String methodName = point.getSignature().getName();
            Class<?>[] parameterTypes = ((MethodSignature) point.getSignature()).getMethod().getParameterTypes();
            Method method = target.getClass().getMethod(methodName, parameterTypes);

            GlobalInterceptor globalInterceptor = method.getAnnotation(GlobalInterceptor.class);
            //是否校验参数
            if (globalInterceptor.checkParams()) {
                validateParam(method, args);
            }

        } catch (Exception e){
            log.error("拦截器异常",e);
        }


    }

    private void validateParam(Method method, Object[] args) {
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            String value = String.valueOf(args[i]);
            VerifyParam annotation = parameter.getAnnotation(VerifyParam.class);
            if (annotation == null) {
                continue;
            }
            if (annotation.required()) {
                if (value.equals("null")) {
                    throw new BusinessException("缺少参数：" + parameter.getName());
                } else {
                    log.info("参数：" + parameter.getName() + ":" + value);
                }
            }
        }
    }

}
