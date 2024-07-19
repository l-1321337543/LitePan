package com.litepan.entity.po;

import java.io.Serializable;
import java.util.Date;
import com.litepan.enums.DateTimePattenEnum;
import com.litepan.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * @Description: 邮箱验证码
 * @date: 2024/07/18
 */
public class EmailCode implements Serializable {

	/**
	 * 邮箱
	 */
	private String email;

	/**
	 * 验证码
	 */
	private String code;

	/**
	 * 创建时间你
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;

	/**
	 * 状态（0：未使用；1：已使用）
	 */
	private Integer status;

	public void setEmail(String email) {
		this.email = email;
	}

	public String getEmail() {
		return email;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Integer getStatus() {
		return status;
	}

	@Override
	public String toString() {
		return "邮箱:" + (email == null ? "空" : email) + 
				"，验证码:" + (code == null ? "空" : code) + 
				"，创建时间你:" + (createTime == null ? "空" : DateUtil.format(createTime, DateTimePattenEnum.YYYY_MM_DD_HH_MM_SS.getPatten())) + 
				"，状态（0：未使用；1：已使用）:" + (status == null ? "空" : status);
	}
}