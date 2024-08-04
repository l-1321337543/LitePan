package com.litepan.service.impl;

import com.litepan.entity.constants.Constants;
import com.litepan.enums.ResponseCodeEnum;
import com.litepan.enums.ShareValidTypeEnums;
import com.litepan.exception.BusinessException;
import com.litepan.service.FileShareService;
import com.litepan.entity.po.FileShare;
import com.litepan.entity.query.FileShareQuery;
import com.litepan.utils.DateUtil;
import com.litepan.utils.StringTools;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

import com.litepan.entity.vo.PaginationResultVO;
import com.litepan.mappers.FileShareMapper;
import com.litepan.entity.query.SimplePage;
import com.litepan.enums.PageSize;
import org.springframework.transaction.annotation.Transactional;


/**
 * @Description: 分享信息表ServiceImpl
 * @date: 2024/08/01
 */
@Service("fileShareService")
public class FileShareServiceImpl implements FileShareService {

    @Resource
    private FileShareMapper<FileShare, FileShareQuery> fileShareMapper;

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveShare(FileShare fileShare) {
        ShareValidTypeEnums validType = ShareValidTypeEnums.getByType(fileShare.getValidType());
        if (validType == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        if (!validType.equals(ShareValidTypeEnums.FOREVER)) {
            fileShare.setExpireTime(DateUtil.getAfterDate(validType.getDays()));
        }
        if (StringTools.isEmpty(fileShare.getCode())) {
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
}