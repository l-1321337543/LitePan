package com.litepan.mappers;

import com.litepan.entity.po.FileShare;
import com.litepan.entity.query.FileShareQuery;
import org.apache.ibatis.annotations.Param;


/**
 * @Description: 分享信息表Mapper
 * @date: 2024/08/01
 */
public interface FileShareMapper<T, P> extends BaseMapper<FileShare, FileShareQuery> {

    /**
     * 根据shareId查询
     */
    T selectByShareId(@Param("shareId") String shareId);

    /**
     * 根据shareId更新
     */
    Integer updateByShareId(@Param("bean") T t, @Param("shareId") String shareId);

    /**
     * 根据shareId删除
     */
    Integer deleteByShareId(@Param("shareId") String shareId);

    Integer delFileShareBatch(@Param("shareIdArray") String[] shareIdArray, @Param("userId") String userId);

    Integer updateShareShowCount(@Param("shareId") String shareId);

}