package com.litepan.entity.po;

import java.io.Serializable;
import java.util.Date;

import com.litepan.enums.DateTimePattenEnum;
import com.litepan.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * @Description: 分享信息表
 * @date: 2024/08/01
 */
public class FileShare implements Serializable {

    /**
     * 分享id
     */
    private String shareId;

    /**
     * 文件id
     */
    private String fileId;

    /**
     * 分享人id
     */
    private String userId;

    /**
     * 有效期(0:1天;1:7天;2:30天;3:永久)
     */
    private Integer validType;

    /**
     * 失效时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date expireTime;

    /**
     * 分享时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date shareTime;

    /**
     * 提取码
     */
    private String code;

    /**
     * 浏览次数
     */
    private Integer showCount;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件封面
     */
    private String fileCover;

    /**
     * 文件类型(0:文件;1:目录)
     */
    private Integer folderType;

    /**
     * 文件分类(1:视频;2:音频;3:图片;4:文档;5:其他)
     */
    private Integer fileCategory;

    /**
     * 文件分类(1:视频;2:音频;3:图片;4:pdf;5:doc;6:excel;7:txt;8:code;9:zip;10:其他)
     */
    private Integer fileType;

    public String getFileCover() {
        return fileCover;
    }

    public void setFileCover(String fileCover) {
        this.fileCover = fileCover;
    }

    public Integer getFolderType() {
        return folderType;
    }

    public void setFolderType(Integer folderType) {
        this.folderType = folderType;
    }

    public Integer getFileCategory() {
        return fileCategory;
    }

    public void setFileCategory(Integer fileCategory) {
        this.fileCategory = fileCategory;
    }

    public Integer getFileType() {
        return fileType;
    }

    public void setFileType(Integer fileType) {
        this.fileType = fileType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setShareId(String shareId) {
        this.shareId = shareId;
    }

    public String getShareId() {
        return shareId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getFileId() {
        return fileId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setValidType(Integer validType) {
        this.validType = validType;
    }

    public Integer getValidType() {
        return validType;
    }

    public void setExpireTime(Date expireTime) {
        this.expireTime = expireTime;
    }

    public Date getExpireTime() {
        return expireTime;
    }

    public void setShareTime(Date shareTime) {
        this.shareTime = shareTime;
    }

    public Date getShareTime() {
        return shareTime;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setShowCount(Integer showCount) {
        this.showCount = showCount;
    }

    public Integer getShowCount() {
        return showCount;
    }

    @Override
    public String toString() {
        return "分享id:" + (shareId == null ? "空" : shareId) +
                "，文件id:" + (fileId == null ? "空" : fileId) +
                "，分享人id:" + (userId == null ? "空" : userId) +
                "，有效期(0:1天;1:7天;2:30天;3:永久):" + (validType == null ? "空" : validType) +
                "，失效时间:" + (expireTime == null ? "空" : DateUtil.format(expireTime, DateTimePattenEnum.YYYY_MM_DD_HH_MM_SS.getPatten())) +
                "，分享时间:" + (shareTime == null ? "空" : DateUtil.format(shareTime, DateTimePattenEnum.YYYY_MM_DD_HH_MM_SS.getPatten())) +
                "，提取码:" + (code == null ? "空" : code) +
                "，浏览次数:" + (showCount == null ? "空" : showCount);
    }
}