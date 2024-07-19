package com.litepan.mappers;

import com.litepan.entity.po.UserInfo;
import com.litepan.entity.query.UserInfoQuery;
import org.apache.ibatis.annotations.Param;


/**
 * @Description: 用户信息Mapper
 * @date: 2024/07/09
 */
public interface UserInfoMapper<T, P> extends BaseMapper<UserInfo, UserInfoQuery> {

    /**
     * 根据userId查询
     */
    T selectByUserId(@Param("userId") String userId);

    /**
     * 根据userId更新
     */
    Integer updateByUserId(@Param("bean") T t, @Param("userId") String userId);

    /**
     * 根据userId删除
     */
    Integer deleteByUserId(@Param("userId") String userId);

    /**
     * 根据email查询
     */
    T selectByEmail(@Param("email") String email);

    /**
     * 根据email更新
     */
    Integer updateByEmail(@Param("bean") T t, @Param("email") String email);

    /**
     * 根据email删除
     */
    Integer deleteByEmail(@Param("email") String email);

    /**
     * 根据qqOpenId查询
     */
    T selectByQqOpenId(@Param("qqOpenId") String qqOpenId);

    /**
     * 根据qqOpenId更新
     */
    Integer updateByQqOpenId(@Param("bean") T t, @Param("qqOpenId") String qqOpenId);

    /**
     * 根据qqOpenId删除
     */
    Integer deleteByQqOpenId(@Param("qqOpenId") String qqOpenId);

    /**
     * 根据nickName查询
     */
    T selectByNickName(@Param("nickName") String nickName);

    /**
     * 根据nickName更新
     */
    Integer updateByNickName(@Param("bean") T t, @Param("nickName") String nickName);

    /**
     * 根据nickName删除
     */
    Integer deleteByNickName(@Param("nickName") String nickName);

}