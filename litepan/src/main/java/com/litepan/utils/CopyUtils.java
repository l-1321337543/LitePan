package com.litepan.utils;

import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 利用spring提供的BeenUtils工具类实现集合元素映射以及单个对象映射
 */
public class CopyUtils {

    /**
     * 这个方法用于将一个列表List<S>中的每个元素复制成另一个类型的列表List<T>
     *
     * @param list  源类型的list列表
     * @param clazz 目标类型的class类型
     * @param <T>   目标类型
     * @param <S>   源类型
     * @return 替换元素之后的list列表
     */
    public static <T, S> List<T> copyList(List<S> list, Class<T> clazz) {
        return list.stream().map(s -> {
            T t = null;
            try {
                t = clazz.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            BeanUtils.copyProperties(s, t);
            return t;
        }).collect(Collectors.toList());
    }

    public static <T, S> T copy(S s, Class<T> clazz) {
        T t = null;
        try {
            t = clazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        BeanUtils.copyProperties(s, t);
        return t;
    }
}
