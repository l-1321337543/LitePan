package com.litepan.entity.dto;

import lombok.Data;

/**
 * @author 李臣洋
 * @version 1.0
 * @description: 表示层用户DTO
 * @date 2024/7/24 15:15
 */
@Data
public class SessionWebUserDTO {
    private String userId;
    //    private String email;
    private String nickName;
    private boolean isAdmin = false;// 默认为false
    private String avatar;
}