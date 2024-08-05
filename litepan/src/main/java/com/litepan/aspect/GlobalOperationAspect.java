package com.litepan.aspect;

import com.litepan.annotation.GlobalInterceptor;
import com.litepan.annotation.VerifyParam;
import com.litepan.entity.constants.Constants;
import com.litepan.entity.dto.SessionWebUserDTO;
import com.litepan.enums.ResponseCodeEnum;
import com.litepan.exception.BusinessException;
import com.litepan.utils.StringTools;
import com.litepan.utils.VerifyUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Objects;

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

    private static final String[] TYPE_BASE = {"java.lang.String", "java.lang.integer", "java.lang.Long"};

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

            //校验登录
            if (globalInterceptor.checkLogin() || globalInterceptor.checkAdmin()) {
                validateLogin(globalInterceptor.checkAdmin());
            }

            //校验参数
            if (globalInterceptor.checkParams()) {
                validateParam(method, args);
            }

        } catch (BusinessException e) {
            log.error("全局拦截器异常", e);
            throw e;
        } catch (Throwable e) {
            log.error("全局拦截器异常", e);
            throw new BusinessException(ResponseCodeEnum.CODE_500);
        }


    }

    /**
     * 校验登录以及校验管理员
     *
     * @param checkAdmin 是否校验管理员
     */
    private void validateLogin(boolean checkAdmin) {
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        HttpSession session = request.getSession();
        SessionWebUserDTO sessionWebUserDTO = (SessionWebUserDTO) session.getAttribute(Constants.SESSION_KEY);

        // session中拿不到数据，则表示未登录
        if (sessionWebUserDTO == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_901);
        }
        // 如果需要校验管理员，但是登陆的账户并不是管理员账户，则为非法访问
        if (checkAdmin && !sessionWebUserDTO.getIsAdmin()) {
            throw new BusinessException(ResponseCodeEnum.CODE_404);
        }
    }


    /**
     * 对传入方法的参数进行校验
     *
     * @param method 要检验参数的方法
     * @param args   参数值数组
     */
    private void validateParam(Method method, Object[] args) {
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            String value = String.valueOf(args[i]);
            VerifyParam verifyParam = parameter.getAnnotation(VerifyParam.class);
            if (verifyParam == null) {
                continue;
            }

            /*
            根据参数类型分两种情况，参数是基本类型和参数是对象

            有两种方式获取参数类型
            第一种
            这种方式不会获取泛型，例如List<String> strings,获取到的类型为java.util.List
            Class<?> type = parameter.getType();
            String name = type.getName();
            第二种
            这种方式会包含泛型，例如List<String> strings,获取到的类型为java.util.List<java.lang.String>
            String typeName = parameter.getParameterizedType().getTypeName();*/
            if (ArrayUtils.contains(TYPE_BASE, parameter.getParameterizedType().getTypeName())) { //参数是基本类型
                checkValue(verifyParam, value);
            } else { //参数是对象
                checkObjValue(parameter, value);
            }
        }
    }

    //TODO 对象类型参数校验 视频没讲，在论坛项目有
    private void checkObjValue(Parameter parameter, Object value) {
        try {
            String typeName = parameter.getParameterizedType().getTypeName();
            Class clazz = Class.forName(typeName);
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                VerifyParam fieldVerifyParam = field.getAnnotation(VerifyParam.class);
                if (fieldVerifyParam == null) {
                    continue;
                }
                field.setAccessible(true);
                Object resultValue = field.get(value);
                checkValue(fieldVerifyParam, resultValue);
            }
        } catch (BusinessException e) {
            log.error("校验参数失败", e);
            throw e;
        } catch (Exception e) {
            log.error("校验参数失败", e);
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
    }

    /**
     * 对简单类型参数校验
     *
     * @param verifyParam 参数注解对象
     * @param value       参数值
     */
    private void checkValue(VerifyParam verifyParam, Object value) {
        boolean isEmpty = value == null || StringTools.isEmpty(String.valueOf(value));
        int length = value == null ? 0 : value.toString().length();

        //空校验
        if (isEmpty && verifyParam.required()) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        //长度检验
        if (!isEmpty && ((verifyParam.min() != -1 && length < verifyParam.min()) || (verifyParam.max() != -1 && length > verifyParam.max()))) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        //正则校验
        if (!isEmpty && !StringTools.isEmpty(verifyParam.regex().getRegex()) && !VerifyUtils.verify(verifyParam.regex(), String.valueOf(value))) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
    }


}
