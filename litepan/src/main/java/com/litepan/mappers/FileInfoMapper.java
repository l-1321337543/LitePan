package com.litepan.mappers;

import com.litepan.entity.po.FileInfo;
import com.litepan.entity.query.FileInfoQuery;
import org.apache.ibatis.annotations.Param;


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

}