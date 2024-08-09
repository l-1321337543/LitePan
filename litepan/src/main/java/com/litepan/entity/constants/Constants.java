package com.litepan.entity.constants;

/**
 * @author 李臣洋
 * @version 1.0
 * @description: 常量
 * @date 2024/7/18 20:29
 */
public class Constants {

    public static final String ZERO_STR = "0";
    public static final int ZERO = 0;
    public static final int ONE = 1;
    public static final int LENGTH_5 = 5;
    public static final int LENGTH_10 = 10;
    public static final int LENGTH_15 = 15;
    public static final int LENGTH_20 = 20;
    public static final int LENGTH_50 = 50;
    public static final int LENGTH_150 = 150;
    public static final long MB = 1024 * 1024;
    public static final int REDIS_EXPIRES_TIME_ONE_MIN = 60;
    public static final int REDIS_EXPIRES_TIME_FIVE_MIN = REDIS_EXPIRES_TIME_ONE_MIN * 5;
    public static final int REDIS_EXPIRES_TIME_ONE_HOUR = REDIS_EXPIRES_TIME_ONE_MIN * 60;
    public static final int REDIS_EXPIRES_TIME_DAY = REDIS_EXPIRES_TIME_ONE_MIN * 60 * 24;

    public static final String SESSION_KEY = "session_key";
    public static final String SESSION_SHARE_KEY = "session_share_key_";
    public static final String CHECK_CODE_KEY = "check_code_key";
    public static final String CHECK_CODE_KEY_EMAIL = "check_code_key_email";

    public static final String REDIS_KEY_SYS_SETTING = "litepan:syssetting";
    public static final String REDIS_KEY_USER_SPACE_USE = "litepan:user:spaceuse:";
    public static final String REDIS_KEY_USER_FILE_TEMP_SIZE = "litepan:user:file:temp:size:";
    public static final String REDIS_KEY_DOWNLOAD = "litepan:download:";

    public static final String FILE_FOLDER_FILE = "file/";
    public static final String FILE_FOLDER_TEMP = "temp/";
    public static final String FILE_FOLDER_AVATAR_NAME = "avatar/";
    public static final String AVATAR_SUFFIX = ".jpg";
    public static final String IMAGE_PNG_SUFFIX = ".png";
    public static final String AVATAR_DEFAULT = "default_avatar.jpg";
    public static final String TS_NAME = "index.ts";
    public static final String M3U8_NAME = "index.m3u8";
}
