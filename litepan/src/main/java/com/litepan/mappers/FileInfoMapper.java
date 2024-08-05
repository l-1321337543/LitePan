package com.litepan.mappers;

import com.litepan.entity.po.FileInfo;
import com.litepan.entity.query.FileInfoQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * @Description: 文件信息表Mapper
 * @date: 2024/07/26
 */
public interface FileInfoMapper<T, P> extends BaseMapper<FileInfo, FileInfoQuery> {

    /**
     * 根据fileIdAndUserId查询
     */
    T selectByFileIdAndUserId(@Param("fileId") String fileId, @Param("userId") String userId);

    /**
     * 根据fileIdAndUserId更新
     */
    Integer updateByFileIdAndUserId(@Param("bean") T t, @Param("fileId") String fileId, @Param("userId") String userId);

    /**
     * 根据fileIdAndUserId删除
     */
    Integer deleteByFileIdAndUserId(@Param("fileId") String fileId, @Param("userId") String userId);

    /**
     * 根据userId获取用户已使用空间大小
     */
    Long selectUseSpace(@Param("userId") String userID);

    /**
     * 更新文件的状态，并且必须由就的状态变更到新状态
     *
     * @param t         封装新状态信息
     * @param fileId    文件ID
     * @param userId    用户ID
     * @param oldStatus 旧状态
     */
    void updateFileStatusWithOldStatus(@Param("bean") T t, @Param("fileId") String fileId, @Param("userId") String userId, @Param("oldStatus") Integer oldStatus);

    /**
     * 批量更新文件的删除状态
     *
     * @param fileInfo    封装新状态信息
     * @param userId      文件ID
     * @param filePidList 根据文件的FilePid更新
     * @param fileIdList  根据文件的FileId更新
     * @param oldDelFlag  旧的删除状态
     */
    void updateFileDelFlagBatch(@Param("bean") FileInfo fileInfo,
                                @Param("userId") String userId,
                                @Param("filePidList") List<String> filePidList,
                                @Param("fileIdList") List<String> fileIdList,
                                @Param("oldDelFlag") Integer oldDelFlag);

    void delFileBatch(@Param("userId") String userId,
                      @Param("fileIdList") List<String> fileIdList);

    void delFileByUserId(@Param("userId") String userId);
}