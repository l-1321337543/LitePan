package com.litepan.service;

import com.litepan.entity.dto.SessionWebUserDTO;
import com.litepan.entity.dto.UploadResultDTO;
import com.litepan.entity.po.FileInfo;
import com.litepan.entity.query.FileInfoQuery;

import java.util.List;

import com.litepan.entity.vo.PaginationResultVO;
import org.springframework.web.multipart.MultipartFile;


/**
 * @Description: 文件信息表Service
 * @date: 2024/07/26
 */
public interface FileInfoService {

    /**
     * 根据条件查询列表
     */
    List<FileInfo> findListByParam(FileInfoQuery query);

    /**
     * 根据条件查询数量
     */
    Integer findCountByParam(FileInfoQuery query);

    /**
     * 分页查询
     */
    PaginationResultVO<FileInfo> findListByPage(FileInfoQuery query);

    /**
     * 新增
     */
    Integer add(FileInfo bean);

    /**
     * 批量新增
     */
    Integer addBatch(List<FileInfo> listBean);

    /**
     * 批量新增/修改
     */
    Integer addOrUpdateBatch(List<FileInfo> listBean);

    /**
     * 根据fileIdAndUserId查询
     */
    FileInfo getFileInfoByFileIdAndUserId(String fileId, String userId);

    /**
     * 根据fileIdAndUserId更新
     */
    Integer updateFileInfoByFileIdAndUserId(FileInfo bean, String fileId, String userId);

    /**
     * 根据fileIdAndUserId删除
     */
    Integer deleteFileInfoByFileIdAndUserId(String fileId, String userId);

    /**
     * 上传文件
     */
    UploadResultDTO uploadFile(SessionWebUserDTO webUserDTO, String fileId, MultipartFile file,
                               String fileName, String filePid, String fileMD5, Integer chunkIndex, Integer chunks);

    /**
     * 创建文件夹
     */
    FileInfo newFolder(String userId, String folderName, String filePid);

    /**
     * 重命名文件/文件夹
     *
     * @param fileId   文件Id
     * @param fileName 文件名
     * @param userId   用户Id
     * @return 重命名后的FileInfo
     */
    FileInfo rename(String userId, String fileId, String fileName);

    /**
     * 移动文件/文件夹到指定文件夹
     *
     * @param userId  用户Id
     * @param fileIds 前端选中的要移动的文件/文件夹的fileId，以","分隔
     * @param filePid 目标目录的fileId，即将前端选中的要移动的文件/文件夹的filePid改为此值
     */
    void changeFileFolder(String userId, String fileIds, String filePid);

    /**
     * 将文件/文件夹批量放入回收站
     */
    void removeFile2RecycleBatch(String userId, String fileIds);
}