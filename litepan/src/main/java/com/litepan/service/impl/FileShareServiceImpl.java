package com.litepan.service.impl;

import com.litepan.entity.constants.Constants;
import com.litepan.entity.dto.SessionShareDTO;
import com.litepan.entity.po.FileInfo;
import com.litepan.entity.query.FileInfoQuery;
import com.litepan.enums.*;
import com.litepan.exception.BusinessException;
import com.litepan.mappers.FileInfoMapper;
import com.litepan.service.FileShareService;
import com.litepan.entity.po.FileShare;
import com.litepan.entity.query.FileShareQuery;
import com.litepan.utils.DateUtil;
import com.litepan.utils.StringTools;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.litepan.entity.vo.PaginationResultVO;
import com.litepan.mappers.FileShareMapper;
import com.litepan.entity.query.SimplePage;
import org.springframework.transaction.annotation.Transactional;


/**
 * @Description: 分享信息表ServiceImpl
 * @date: 2024/08/01
 */
@Service("fileShareService")
public class FileShareServiceImpl implements FileShareService {

    @Resource
    private FileShareMapper<FileShare, FileShareQuery> fileShareMapper;

    @Resource
    private FileInfoMapper<FileInfo, FileInfoQuery> fileInfoMapper;

    /**
     * 根据条件查询列表
     */
    @Override
    public List<FileShare> findListByParam(FileShareQuery query) {
        return fileShareMapper.selectListByQuery(query);
    }

    /**
     * 根据条件查询数量
     */
    @Override
    public Integer findCountByParam(FileShareQuery query) {
        return fileShareMapper.selectCountByQuery(query);
    }

    /**
     * 分页查询
     */
    @Override
    public PaginationResultVO<FileShare> findListByPage(FileShareQuery query) {
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
    public Integer add(FileShare bean) {
        return fileShareMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<FileShare> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return fileShareMapper.insertBatch(listBean);
    }

    /**
     * 批量新增/修改
     */
    @Override
    public Integer addOrUpdateBatch(List<FileShare> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return fileShareMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 根据shareId查询
     */
    @Override
    public FileShare getFileShareByShareId(String shareId) {
        return fileShareMapper.selectByShareId(shareId);
    }

    /**
     * 根据shareId更新
     */
    @Override
    public Integer updateFileShareByShareId(FileShare bean, String shareId) {
        return fileShareMapper.updateByShareId(bean, shareId);
    }

    /**
     * 根据shareId删除
     */
    @Override
    public Integer deleteFileShareByShareId(String shareId) {
        return fileShareMapper.deleteByShareId(shareId);
    }

    /**
     * 保存分享信息，如果用户没有自己输入提取码，则生成随机五位提取码
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveShareInfo(FileShare fileShare) {
        ShareValidTypeEnums validType = ShareValidTypeEnums.getByType(fileShare.getValidType());
        if (validType == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        if (!validType.equals(ShareValidTypeEnums.FOREVER)) {
            fileShare.setExpireTime(DateUtil.getAfterDate(validType.getDays()));
        }
        if (StringTools.isEmpty(fileShare.getCode())) {
            //生成随机提取码
            fileShare.setCode(StringTools.getRandomString(Constants.LENGTH_5));
        }
        fileShare.setShareTime(new Date());
        fileShare.setShareId(StringTools.getRandomString(Constants.LENGTH_20));
        fileShare.setShowCount(0);
        fileShareMapper.insert(fileShare);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delFileShareBatch(String userId, String shareIds) {
        String[] shareIdArray = shareIds.split(",");
        Integer count = fileShareMapper.delFileShareBatch(shareIdArray, userId);
        if (!count.equals(shareIdArray.length)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SessionShareDTO checkShareCode(String shareId, String code) {
        FileShare fileShare = fileShareMapper.selectByShareId(shareId);
        if (fileShare == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_902);
        }
        if (!fileShare.getCode().equals(code)) {
            throw new BusinessException(ResponseCodeEnum.CODE_904);
        }
        fileShareMapper.updateShareShowCount(shareId);
        SessionShareDTO sessionShareDTO = new SessionShareDTO();
        sessionShareDTO.setFileId(fileShare.getFileId());
        sessionShareDTO.setShareUserId(fileShare.getUserId());
        sessionShareDTO.setExpireTime(fileShare.getExpireTime());
        sessionShareDTO.setShareId(shareId);
        return sessionShareDTO;
    }

    @Override
    public void checkRootFilePid(String userId, String fileId, String filePid) {
        if (filePid.equals(fileId)) {
            return;
        }
        checkFilePid(userId, fileId, filePid);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveShare(String curUserId, String shareUserId, String fileRootPid, String shareFileIds, String folderId) {
        String[] fileIdArray = shareFileIds.split(",");
        //查询要存入的文件夹下的文件
        FileInfoQuery query = new FileInfoQuery();
        query.setFilePid(folderId);
        query.setUserId(curUserId);
        List<FileInfo> targetFoldersFiles = fileInfoMapper.selectListByQuery(query);
        Map<String, FileInfo> targetFoldersFileMap = targetFoldersFiles.stream()
                .collect(Collectors.toMap(FileInfo::getFileName, Function.identity(), (data1, data2) -> data2));

        //被选中的要存入的文件
        query = new FileInfoQuery();
        query.setUserId(shareUserId);
        query.setFileIdArray(fileIdArray);
        List<FileInfo> selectedFiles = fileInfoMapper.selectListByQuery(query);
        List<FileInfo> sharedFiles = new ArrayList<>();
        Date curDate = new Date();
        //如果选中的文件与现有文件命名有冲突，则重命名
        selectedFiles.forEach(selectedFile -> {
            if (targetFoldersFileMap.get(selectedFile.getFileName()) != null) {
                selectedFile.setFileName(StringTools.rename(selectedFile.getFileName()));
            }
            findAllSubFile(sharedFiles, selectedFile, shareUserId, curUserId, curDate, folderId);
        });

        //将所有分享的文件存入数据库
        fileInfoMapper.insertBatch(sharedFiles);
    }

    /**
     * 递归查询所有被选中的要存入的文件及其子文件，然后添加到sharedFiles中
     *
     * @param sharedFiles 所有被选中的要存入的文件及其子文件
     * @param fileInfo    本次要添加的文件
     * @param shareUserId 分享用户id
     * @param curUserId   当前用户Id
     * @param curDate     当前时间
     * @param folderId    要存入的文件夹
     */
    private void findAllSubFile(List<FileInfo> sharedFiles, FileInfo fileInfo,
                                String shareUserId, String curUserId, Date curDate, String folderId) {
        String selectFileId = fileInfo.getFileId();
        fileInfo.setUserId(curUserId);
        fileInfo.setCreateTime(curDate);
        fileInfo.setLastUpdateTime(curDate);
        fileInfo.setFilePid(folderId);
        String newFileId = StringTools.getRandomString(Constants.LENGTH_10);
        fileInfo.setFileId(newFileId);
        sharedFiles.add(fileInfo);

        if (fileInfo.getFolderType().equals(FileFolderTypeEnums.FOLDER.getType())) {
            FileInfoQuery query = new FileInfoQuery();
            query.setFilePid(selectFileId);
            query.setUserId(shareUserId);
            List<FileInfo> subFiles = fileInfoMapper.selectListByQuery(query);
            subFiles.forEach(subFile -> {
                findAllSubFile(sharedFiles, subFile, shareUserId, curUserId, curDate, newFileId);
            });
        }
    }


    /**
     * 校验要获取的文件夹是否是被分享的文件夹的子文件
     */
    private void checkFilePid(String userId, String fileId, String filePid) {
        FileInfo fileInfo = fileInfoMapper.selectByFileIdAndUserId(filePid, userId);
        if (fileInfo == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        if (fileInfo.getFilePid().equals(Constants.ZERO_STR)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        if (fileInfo.getFilePid().equals(fileId)) {
            return;
        }
        checkFilePid(userId, fileId, fileInfo.getFilePid());
    }
}