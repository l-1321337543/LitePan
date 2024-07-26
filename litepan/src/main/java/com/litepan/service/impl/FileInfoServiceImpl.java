package com.litepan.service.impl;

import com.litepan.entity.constants.Constants;
import com.litepan.entity.dto.SessionWebUserDTO;
import com.litepan.entity.dto.UploadResultDTO;
import com.litepan.entity.dto.UserSpaceDTO;
import com.litepan.entity.po.UserInfo;
import com.litepan.entity.query.UserInfoQuery;
import com.litepan.enums.*;
import com.litepan.exception.BusinessException;
import com.litepan.mappers.UserInfoMapper;
import com.litepan.service.FileInfoService;
import com.litepan.entity.po.FileInfo;
import com.litepan.entity.query.FileInfoQuery;
import com.litepan.utils.RedisComponent;
import com.litepan.utils.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

import com.litepan.entity.vo.PaginationResultVO;
import com.litepan.mappers.FileInfoMapper;
import com.litepan.entity.query.SimplePage;
import org.springframework.web.multipart.MultipartFile;


/**
 * @Description: 文件信息表ServiceImpl
 * @date: 2024/07/26
 */
@Service("fileInfoService")
public class FileInfoServiceImpl implements FileInfoService {

    @Resource
    private FileInfoMapper<FileInfo, FileInfoQuery> fileInfoMapper;

    @Resource
    private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

    @Resource
    private RedisComponent redisComponent;

    /**
     * 根据条件查询列表
     */
    @Override
    public List<FileInfo> findListByParam(FileInfoQuery query) {
        return fileInfoMapper.selectListByQuery(query);
    }

    /**
     * 根据条件查询数量
     */
    @Override
    public Integer findCountByParam(FileInfoQuery query) {
        return fileInfoMapper.selectCountByQuery(query);
    }

    /**
     * 分页查询
     */
    @Override
    public PaginationResultVO<FileInfo> findListByPage(FileInfoQuery query) {
        Integer count = this.findCountByParam(query);
        int pageSize = query.getPageSize() == null ? PageSize.SIZE15.getSize() : query.getPageSize();
        SimplePage page = new SimplePage(query.getPageNo(), count, pageSize);
        query.setSimplePage(page);
        return new PaginationResultVO<>(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), this.findListByParam(query));
    }

    /**
     * 新增
     */
    @Override
    public Integer add(FileInfo bean) {
        return fileInfoMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<FileInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return fileInfoMapper.insertBatch(listBean);
    }

    /**
     * 批量新增/修改
     */
    @Override
    public Integer addOrUpdateBatch(List<FileInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return fileInfoMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 根据fileIdAndUserId查询
     */
    @Override
    public FileInfo getFileInfoByFileIdAndUserId(String fileId, String userId) {
        return fileInfoMapper.selectByFileIdAndUserId(fileId, userId);
    }

    /**
     * 根据fileIdAndUserId更新
     */
    @Override
    public Integer updateFileInfoByFileIdAndUserId(FileInfo bean, String fileId, String userId) {
        return fileInfoMapper.updateByFileIdAndUserId(bean, fileId, userId);
    }

    /**
     * 根据fileIdAndUserId删除
     */
    @Override
    public Integer deleteFileInfoByFileIdAndUserId(String fileId, String userId) {
        return fileInfoMapper.deleteByFileIdAndUserId(fileId, userId);
    }

    @Override
    public UploadResultDTO uploadFile(SessionWebUserDTO webUserDTO, String fileId, MultipartFile file,
                                      String fileName, String filePid, String fileMD5, Integer chunkIndex, Integer chunks) {
        UploadResultDTO uploadResultDTO = new UploadResultDTO();
        if (fileId == null) {
            fileId = StringUtils.getRandomNumber(Constants.LENGTH_10);
        }
        uploadResultDTO.setFileId(fileId);

        Date curDate = new Date();
        UserSpaceDTO userSpaceUse = redisComponent.getUserSpaceUse(webUserDTO.getUserId());

        //第一快
        if (chunkIndex == 0) {

            FileInfoQuery infoQuery = new FileInfoQuery();
            infoQuery.setFileMd5(fileMD5);
            infoQuery.setStatus(FileStatusEnums.TRANSFER_SUCCESS.getStatus());
            infoQuery.setSimplePage(new SimplePage(0, 1));
            List<FileInfo> dbFileInfos = fileInfoMapper.selectListByQuery(infoQuery);

            //秒传
            if (!dbFileInfos.isEmpty()) {
                FileInfo dbFileInfo = dbFileInfos.get(0);

                //判断文件大小
                if (dbFileInfo.getFileSize() + userSpaceUse.getUseSpace() > userSpaceUse.getTotalSpace()) {
                    throw new BusinessException(ResponseCodeEnum.CODE_904);
                }
                fileName = autoRename(filePid, webUserDTO.getUserId(), fileName);
                dbFileInfo.setFileId(fileId)
                        .setFilePid(filePid)
                        .setUserId(webUserDTO.getUserId())
                        .setCreateTime(curDate)
                        .setLastUpdateTime(curDate)
                        .setDelFlag(FileDelFlagEnums.NORMAL.getFlag())
                        .setFileName(fileName)
                        .setStatus(FileStatusEnums.TRANSFER_SUCCESS.getStatus());

                //更新数据库
                fileInfoMapper.insert(dbFileInfo);

                uploadResultDTO.setStatus(UploadStatusEnums.UPLOAD_SECONDS.getCode());
                //用户空间使用情况
                updateUseSpace(webUserDTO.getUserId(), dbFileInfo.getFileSize());
                return uploadResultDTO;
            }


        }


        return uploadResultDTO;
    }

    /**
     * 从文件信息表中根据filePid，userId，fileName查询是否存在文件正常的记录，若存在，则重命名，不存在则无需重命名
     */
    private String autoRename(String filePid, String userId, String fileName) {
        FileInfoQuery fileInfoQuery = new FileInfoQuery();
        fileInfoQuery.setFilePid(filePid);
        fileInfoQuery.setUserId(userId);
        fileInfoQuery.setDelFlag(FileDelFlagEnums.NORMAL.getFlag());
        fileInfoQuery.setFileName(fileName);
        Integer count = fileInfoMapper.selectCountByQuery(fileInfoQuery);

        //需要重命名
        if (count > 0) {
            fileName = StringUtils.rename(fileName);
        }
        return fileName;
    }

    /**
     * 更新用户空间使用情况
     *
     * @param userId   更新的用户
     * @param useSpace 变更的空间大小，可正可负
     */
    private void updateUseSpace(String userId, Long useSpace) {
        Integer count = userInfoMapper.updateUseSpaceByUserId(userId, useSpace, null);
        if (count == 0) {
            throw new BusinessException(ResponseCodeEnum.CODE_904);
        }
        UserSpaceDTO userSpaceDTO = redisComponent.getUserSpaceUse(userId);
        userSpaceDTO.setUseSpace(userSpaceDTO.getUseSpace() + useSpace);
        redisComponent.saveUserSpaceUse(userId, userSpaceDTO);
    }
}