package com.litepan.utils;

import com.litepan.entity.constants.Constants;
import com.litepan.entity.dto.DownloadFileDTO;
import com.litepan.entity.dto.SysSettingDTO;
import com.litepan.entity.dto.UserSpaceDTO;
import com.litepan.entity.po.FileInfo;
import com.litepan.entity.po.UserInfo;
import com.litepan.entity.query.FileInfoQuery;
import com.litepan.entity.query.UserInfoQuery;
import com.litepan.mappers.FileInfoMapper;
import com.litepan.mappers.UserInfoMapper;
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

    @Resource
    private FileInfoMapper<FileInfo, FileInfoQuery> fileInfoMapper;

    @Resource
    private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

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


    public SysSettingDTO saveSysSettingDTO(SysSettingDTO sysSettingDTO) {
        redisUtils.set(Constants.REDIS_KEY_SYS_SETTING, sysSettingDTO);
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
            Long useSpace = fileInfoMapper.selectUseSpace(userId);
            userSpaceDTO.setUseSpace(useSpace);
            userSpaceDTO.setTotalSpace(getSysSettingDTO().getUserInitUseSpace());
            saveUserSpaceUse(userId, userSpaceDTO);
        }
        return userSpaceDTO;
    }

    /**
     * 向Redis中存储将要上传文件的大小，因为是分片上传，后上传的"片"的文件大小会加到已上传部分上
     */
    public void saveFileTempSize(String userId, String fileId, Long fileSize) {
        Long curFileSize = getFileTempSize(userId, fileId);
        redisUtils.setEx(Constants.REDIS_KEY_USER_FILE_TEMP_SIZE + userId + fileId,
                curFileSize + fileSize,
                Constants.REDIS_EXPIRES_TIME_ONE_HOUR);
    }

    /**
     * 从Redis中获取已经上传文件的大小，因为是分片上传，因此获取的是该文件已上传的所有分片的大小总和
     */
    public Long getFileTempSize(String userId, String fileId) {
        return getFileSizeFromRedis(Constants.REDIS_KEY_USER_FILE_TEMP_SIZE + userId + fileId);
    }

    /**
     * 从缓存中获取文件大小，找不到默认返回0
     */
    private Long getFileSizeFromRedis(String key) {
        Object fileSize = redisUtils.get(key);
        if (fileSize == null) {
            return 0L;
        }
        if (fileSize instanceof Integer) {
            return ((Integer) fileSize).longValue();
        } else if (fileSize instanceof Long) {
            return (Long) fileSize;
        }
        return 0L;
    }

    /**
     * 将文件下载需要校验的code存入缓存，并设置五分钟过期
     */
    public void saveDownloadCode(String code, DownloadFileDTO downloadFileDTO) {
        redisUtils.setEx(Constants.REDIS_KEY_DOWNLOAD + code, downloadFileDTO, Constants.REDIS_EXPIRES_TIME_FIVE_MIN);
    }

    /**
     * 从缓存中获取封装的下载信息
     */
    public DownloadFileDTO getDownloadDTO(String code) {
        return (DownloadFileDTO) redisUtils.get(Constants.REDIS_KEY_DOWNLOAD + code);
    }

    public void saveUserSpaceUseByAdmin(String userId) {
        UserSpaceDTO userSpaceDTO = new UserSpaceDTO();
        userSpaceDTO.setUseSpace(fileInfoMapper.selectUseSpace(userId));
        userSpaceDTO.setTotalSpace(userInfoMapper.selectByUserId(userId).getTotalSpace());
        redisUtils.setEx(Constants.REDIS_KEY_USER_SPACE_USE + userId, userSpaceDTO, Constants.REDIS_EXPIRES_TIME_DAY);
    }
}
