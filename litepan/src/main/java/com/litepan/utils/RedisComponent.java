package com.litepan.utils;

import com.litepan.entity.constants.Constants;
import com.litepan.entity.dto.SysSettingDTO;
import com.litepan.entity.dto.UserSpaceDTO;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author 李臣洋
 * @version 1.0
 * @description: 缓存读取和写入
 * @date 2024/7/19 10:12
 */
@Component
public class RedisComponent {
    @Resource
    private RedisUtils<Object> redisUtils;

    /**
     * 从缓存获取邮件内容，获取不到就将邮件的内容存入redis
     *
     * @return 系统设置DTO
     */
    public SysSettingDTO getSysSettingDTO() {
        SysSettingDTO sysSettingDTO = (SysSettingDTO) redisUtils.get(Constants.REDIS_KEY_SYS_SETTING);
        if (sysSettingDTO == null) {
            sysSettingDTO = new SysSettingDTO();
            redisUtils.set(Constants.REDIS_KEY_SYS_SETTING, sysSettingDTO);
        }
        return sysSettingDTO;
    }

    /**
     * 向缓存中存入用户空间使用情况,并设置有效期
     */
    public void saveUserSpaceUse(String userId, UserSpaceDTO userSpaceDTO) {
        redisUtils.setEx(Constants.REDIS_KEY_USER_SPACE_USE + userId, userSpaceDTO, Constants.REDIS_EXPIRES_TIME_DAY);
    }

    /**
     * 从缓存中读取用户空间使用情况,读入不到就先写入
     */
    public UserSpaceDTO getUserSpaceUse(String userId) {
        UserSpaceDTO userSpaceDTO = (UserSpaceDTO) redisUtils.get(Constants.REDIS_KEY_USER_SPACE_USE + userId);
        if (userSpaceDTO == null) {
            userSpaceDTO = new UserSpaceDTO();
            //TODO 查询文件表，获取空间具体使用
            userSpaceDTO.setUseSpace(0L);
            userSpaceDTO.setTotalSpace(getSysSettingDTO().getUserInitUseSpace());
            saveUserSpaceUse(userId,userSpaceDTO);
        }
        return userSpaceDTO;
    }
}
