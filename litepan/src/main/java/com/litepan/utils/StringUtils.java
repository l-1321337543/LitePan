package com.litepan.utils;

import org.apache.commons.lang3.RandomStringUtils;

/**
 * @author 李臣洋
 * @version 1.0
 * @description: 字符串工具类
 * @date 2024/7/18 21:19
 */
public class StringUtils {

    /**
     * 生成指定长度的随机数
     *
     * @param count 指定的长度
     * @return 生成的随机数
     */
    public static String getRandomNumber(int count) {
//        StringBuilder builder = new StringBuilder();
//        Random random = new Random();
//        for (int i = 0; i < 5; i++) {
//            builder.append(random.nextInt(10));
//        }
//        return builder.toString();
//        return RandomStringUtils.random(count, true, false);
        return RandomStringUtils.randomNumeric(count);
    }
}
