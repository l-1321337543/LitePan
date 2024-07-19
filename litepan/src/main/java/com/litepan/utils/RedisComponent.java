package com.litepan.utils;

import com.litepan.entity.constants.Constants;
import com.litepan.entity.dto.SysSettingDto;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author 李臣洋
 * @version 1.0
 * @description: TODO
 * @date 2024/7/19 10:12
 */
@Component
public class RedisComponent {
    @Resource
    private RedisUtils<Object> redisUtils;

    /**
     * 从缓存获取邮件内容，获取不到就将邮件的内容存入redis
     *
     * @return 邮件内容
     */
    public SysSettingDto getSysSettingDto() {
        SysSettingDto sysSettingDto = (SysSettingDto) redisUtils.get(Constants.REDIS_KEY_SYS_SETTING);
        if (sysSettingDto == null) {
            sysSettingDto = new SysSettingDto();
            redisUtils.set(Constants.REDIS_KEY_SYS_SETTING, sysSettingDto);
        }
        return sysSettingDto;
    }
}
