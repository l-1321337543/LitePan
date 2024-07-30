package com.litepan.entity.dto;

import lombok.Data;

@Data
public class DownloadFileDTO {
    private String downloadCode;
    private String fileName;
    private String filePath;
}
