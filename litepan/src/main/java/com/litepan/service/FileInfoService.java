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

}