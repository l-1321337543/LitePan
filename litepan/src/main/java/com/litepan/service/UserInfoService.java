package com.litepan.service;

import com.litepan.entity.dto.SessionWebUserDTO;
import com.litepan.entity.po.UserInfo;
import com.litepan.entity.query.UserInfoQuery;

import java.util.List;

import com.litepan.entity.vo.PaginationResultVO;


/**
 * @Description: 用户信息Service
 * @date: 2024/07/09
 */
public interface UserInfoService {

    /**
     * 根据条件查询列表
     */
    List<UserInfo> findListByParam(UserInfoQuery query);

    /**
     * 根据条件查询数量
     */
    Integer findCountByParam(UserInfoQuery query);

    /**
     * 分页查询
     */
    PaginationResultVO<UserInfo> findListByPage(UserInfoQuery query);

    /**
     * 新增
     */
    Integer add(UserInfo bean);

    /**
     * 批量新增
     */
    Integer addBatch(List<UserInfo> listBean);

    /**
     * 批量新增/修改
     */
    Integer addOrUpdateBatch(List<UserInfo> listBean);

    /**
     * 根据userId查询
     */
    UserInfo getUserInfoByUserId(String userId);

    /**
     * 根据userId更新
     */
    Integer updateUserInfoByUserId(UserInfo bean, String userId);

    /**
     * 根据userId删除
     */
    Integer deleteUserInfoByUserId(String userId);

    /**
     * 根据email查询
     */
    UserInfo getUserInfoByEmail(String email);

    /**
     * 根据email更新
     */
    Integer updateUserInfoByEmail(UserInfo bean, String email);

    /**
     * 根据email删除
     */
    Integer deleteUserInfoByEmail(String email);

    /**
     * 根据qqOpenId查询
     */
    UserInfo getUserInfoByQqOpenId(String qqOpenId);

    /**
     * 根据qqOpenId更新
     */
    Integer updateUserInfoByQqOpenId(UserInfo bean, String qqOpenId);

    /**
     * 根据qqOpenId删除
     */
    Integer deleteUserInfoByQqOpenId(String qqOpenId);

    /**
     * 根据nickName查询
     */
    UserInfo getUserInfoByNickName(String nickName);

    /**
     * 根据nickName更新
     */
    Integer updateUserInfoByNickName(UserInfo bean, String nickName);

    /**
     * 根据nickName删除
     */
    Integer deleteUserInfoByNickName(String nickName);

    /**
     * 用户注册
     */
    void register(String email, String nickName, String password, String emailCode);

    /**
     * 用户登录
     */
    SessionWebUserDTO login(String email, String password);


}