package com.litepan.utils;

import com.litepan.enums.VerifyRegexEnum;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 李臣洋
 * @version 1.0
 * @description: 校验工具类
 * @date 2024/7/23 10:16
 */
public class VerifyUtils {
    public static boolean verify(String regs, String value) {
        if (StringTools.isEmpty(value)) {
            return false;
        }
        Pattern pattern = Pattern.compile(regs);
        Matcher matcher = pattern.matcher(value);
        return matcher.matches();
    }

    public static boolean verify(VerifyRegexEnum verifyRegexEnum, String value) {
        return verify(verifyRegexEnum.getRegex(), value);
    }

}
