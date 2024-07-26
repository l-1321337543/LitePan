package com.litepan.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;

/**
 * @author 李臣洋
 * @version 1.0
 * @description: 文件上传过程中返回给前端的信息
 * @date 2024/7/26 23:41
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class UploadResultDTO implements Serializable {
    private String fileId;
    private String status;
}
