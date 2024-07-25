package com.litepan.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * @author 李臣洋
 * @version 1.0
 * @description: 用户空间DTO
 * @date 2024/7/24 15:24
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserSpaceDTO implements Serializable {
    private long useSpace;
    private long totalSpace;

    public long getUseSpace() {
        return useSpace;
    }

    public void setUseSpace(long useSpace) {
        this.useSpace = useSpace;
    }

    public long getTotalSpace() {
        return totalSpace;
    }

    public void setTotalSpace(long totalSpace) {
        this.totalSpace = totalSpace;
    }
}
