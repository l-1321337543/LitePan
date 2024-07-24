package com.litepan.service;

import com.litepan.entity.po.EmailCode;
import com.litepan.entity.query.EmailCodeQuery;
import java.util.List;
import com.litepan.entity.vo.PaginationResultVO;


/**
 * @Description: 邮箱验证码Service
 * @date: 2024/07/18
 */
public interface EmailCodeService {

	/**
	 * 根据条件查询列表
	 */
	List<EmailCode> findListByParam(EmailCodeQuery query);

	/**
	 * 根据条件查询数量
	 */
	Integer findCountByParam(EmailCodeQuery query);

	/**
	 * 分页查询
	 */
	PaginationResultVO<EmailCode> findListByPage(EmailCodeQuery query);

	/**
	 * 新增
	 */
	Integer add(EmailCode bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<EmailCode> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<EmailCode> listBean);

	/**
	 * 根据emailAndCode查询
	 */
	EmailCode getEmailCodeByEmailAndCode(String email, String code);

	/**
	 * 根据emailAndCode更新
	 */
	Integer updateEmailCodeByEmailAndCode(EmailCode bean, String email, String code);

	/**
	 * 根据emailAndCode删除
	 */
	Integer deleteEmailCodeByEmailAndCode(String email, String code);

	/**
	 * 发送邮箱验证码
	 */
	void sendEmailCode(String email,Integer type);

	/**
	 * 校验邮箱验证码
	 */
	void checkCode(String email,String code);

}