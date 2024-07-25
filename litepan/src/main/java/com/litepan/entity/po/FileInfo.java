package com.litepan.entity.po;

import java.io.Serializable;
import java.util.Date;

import com.litepan.enums.DateTimePattenEnum;
import com.litepan.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * @Description: 文件信息表
 * @date: 2024/07/26
 */
public class FileInfo implements Serializable {

    /**
     * 文件ID
     */
    private String fileId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 文件MD5值
     */
    private String fileMd5;

    /**
     * 父级ID
     */
    private String filePid;

    /**
     * 文件大小
     */
    private Long fileSize;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件封面
     */
    private String fileCover;

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 文件创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 文件最后修改时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastUpdateTime;

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

    /**
     * 文件状态(0:转码中;1:转码失败;2:转码成功;)
     */
    private Integer status;

    /**
     * 进入回收站的时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date recoverTime;

    /**
     * 删除标记(0:删除;1:回收站;2:正常;)
     */
    private Integer delFlag;

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

    public void setFileMd5(String fileMd5) {
        this.fileMd5 = fileMd5;
    }

    public String getFileMd5() {
        return fileMd5;
    }

    public void setFilePid(String filePid) {
        this.filePid = filePid;
    }

    public String getFilePid() {
        return filePid;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileCover(String fileCover) {
        this.fileCover = fileCover;
    }

    public String getFileCover() {
        return fileCover;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setLastUpdateTime(Date lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public Date getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setFolderType(Integer folderType) {
        this.folderType = folderType;
    }

    public Integer getFolderType() {
        return folderType;
    }

    public void setFileCategory(Integer fileCategory) {
        this.fileCategory = fileCategory;
    }

    public Integer getFileCategory() {
        return fileCategory;
    }

    public void setFileType(Integer fileType) {
        this.fileType = fileType;
    }

    public Integer getFileType() {
        return fileType;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getStatus() {
        return status;
    }

    public void setRecoverTime(Date recoverTime) {
        this.recoverTime = recoverTime;
    }

    public Date getRecoverTime() {
        return recoverTime;
    }

    public void setDelFlag(Integer delFlag) {
        this.delFlag = delFlag;
    }

    public Integer getDelFlag() {
        return delFlag;
    }

    @Override
    public String toString() {
        return "文件ID:" + (fileId == null ? "空" : fileId) +
                "，用户ID:" + (userId == null ? "空" : userId) +
                "，文件MD5值:" + (fileMd5 == null ? "空" : fileMd5) +
                "，父级ID:" + (filePid == null ? "空" : filePid) +
                "，文件大小:" + (fileSize == null ? "空" : fileSize) +
                "，文件名:" + (fileName == null ? "空" : fileName) +
                "，文件封面:" + (fileCover == null ? "空" : fileCover) +
                "，文件路径:" + (filePath == null ? "空" : filePath) +
                "，文件创建时间:" + (createTime == null ? "空" : DateUtil.format(createTime, DateTimePattenEnum.YYYY_MM_DD_HH_MM_SS.getPatten())) +
                "，文件最后修改时间:" + (lastUpdateTime == null ? "空" : DateUtil.format(lastUpdateTime, DateTimePattenEnum.YYYY_MM_DD_HH_MM_SS.getPatten())) +
                "，文件类型(0:文件;1:目录):" + (folderType == null ? "空" : folderType) +
                "，文件分类(1:视频;2:音频;3:图片;4:文档;5:其他):" + (fileCategory == null ? "空" : fileCategory) +
                "，文件分类(1:视频;2:音频;3:图片;4:pdf;5:doc;6:excel;7:txt;8:code;9:zip;10:其他):" + (fileType == null ? "空" : fileType) +
                "，文件状态(0:转码中;1:转码失败;2:转码成功;):" + (status == null ? "空" : status) +
                "，进入回收站的时间:" + (recoverTime == null ? "空" : DateUtil.format(recoverTime, DateTimePattenEnum.YYYY_MM_DD_HH_MM_SS.getPatten())) +
                "，删除标记(0:删除;1:回收站;2:正常;):" + (delFlag == null ? "空" : delFlag);
    }
}