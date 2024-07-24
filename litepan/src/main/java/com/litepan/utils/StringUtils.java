package com.litepan.utils;

import org.apache.commons.codec.digest.DigestUtils;
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
        return RandomStringUtils.randomNumeric(count);
    }

    /**
     * 判断传入的字符串是否为空
     *
     * @param str 要判断的字符串
     * @return true：为空；false：不为空
     */
    public static boolean isEmpty(String str) {

        if (null == str || "".equals(str) || "null".equals(str) || "\u0000".equals(str)) {
            return true;
        } else if ("".equals(str.trim())) {
            return true;
        }
        return false;
    }

    /**
     * 将传入的字符串进行MD5加密
     *
     * @return 加密后的字符串
     */
    public static String encodeBuMd5(String originString) {
        return isEmpty(originString) ? null : DigestUtils.md5Hex(originString);
    }

    /**
     * 判断传入的文件路径字符串是否正确
     */
    public static boolean pathIsOk(String path) {
        if (StringUtils.isEmpty(path)) {
            return true;
        }
        if (path.contains("../") || path.contains("..\\")) {
            return false;
        }
        return true;
    }
}
