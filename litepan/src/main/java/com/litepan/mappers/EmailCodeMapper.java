package com.litepan.mappers;

import com.litepan.entity.po.EmailCode;
import com.litepan.entity.query.EmailCodeQuery;
import org.apache.ibatis.annotations.Param;


/**
 * @Description: 邮箱验证码Mapper
 * @date: 2024/07/18
 */
public interface EmailCodeMapper<T, P> extends BaseMapper<EmailCode, EmailCodeQuery> {

    /**
     * 根据emailAndCode查询
     */
    T selectByEmailAndCode(@Param("email") String email, @Param("code") String code);

    /**
     * 根据emailAndCode更新
     */
    Integer updateByEmailAndCode(@Param("bean") T t, @Param("email") String email, @Param("code") String code);

    /**
     * 根据emailAndCode删除
     */
    Integer deleteByEmailAndCode(@Param("email") String email, @Param("code") String code);

    /**
     * 将过时的验证码置为无效
     * @param email 邮箱账号
     */
    void disableEmailCode(@Param("email") String email);

}