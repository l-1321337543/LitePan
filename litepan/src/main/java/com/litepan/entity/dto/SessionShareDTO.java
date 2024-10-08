package com.litepan.entity.dto;

import lombok.Data;

import java.util.Date;

@Data
public class SessionShareDTO {
    private String shareId;
    private String shareUserId;
    private Date expireTime;
    private String fileId;
}
