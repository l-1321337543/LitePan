package com.litepan.entity.po;

import java.io.Serializable;
import java.util.Date;
import com.litepan.enums.DateTimePattenEnum;
import com.litepan.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @Description: 用户信息
 * @date: 2024/07/09
 */
public class UserInfo implements Serializable {

	/**
	 * 用户ID
	 */
	private String userId;

	/**
	 * 用户昵称
	 */
	@JsonIgnore
	private String nickName;

	/**
	 * 邮箱
	 */
	private String email;

	/**
	 * qqOpenId
	 */
	private String qqOpenId;

	/**
	 * qq头像
	 */
	private String qqAvatar;

	/**
	 * 密码
	 */
	private String password;

	/**
	 * 注册时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date joinTime;

	/**
	 * 最后登录时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date lastLoginTime;

	/**
	 * 是否启用（0:禁用; 1:启用）
	 */
	@JsonIgnore
	private Integer status;

	/**
	 * 使用空间（单位：byte）
	 */
	private Long useSpace;

	/**
	 * 总空间（单位：byte）
	 */
	private Long totalSpace;

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserId() {
		return userId;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public String getNickName() {
		return nickName;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getEmail() {
		return email;
	}

	public void setQqOpenId(String qqOpenId) {
		this.qqOpenId = qqOpenId;
	}

	public String getQqOpenId() {
		return qqOpenId;
	}

	public void setQqAvatar(String qqAvatar) {
		this.qqAvatar = qqAvatar;
	}

	public String getQqAvatar() {
		return qqAvatar;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPassword() {
		return password;
	}

	public void setJoinTime(Date joinTime) {
		this.joinTime = joinTime;
	}

	public Date getJoinTime() {
		return joinTime;
	}

	public void setLastLoginTime(Date lastLoginTime) {
		this.lastLoginTime = lastLoginTime;
	}

	public Date getLastLoginTime() {
		return lastLoginTime;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Integer getStatus() {
		return status;
	}

	public void setUseSpace(Long useSpace) {
		this.useSpace = useSpace;
	}

	public Long getUseSpace() {
		return useSpace;
	}

	public void setTotalSpace(Long totalSpace) {
		this.totalSpace = totalSpace;
	}

	public Long getTotalSpace() {
		return totalSpace;
	}

	@Override
	public String toString() {
		return "用户ID:" + (userId == null ? "空" : userId) + 
				"，用户昵称:" + (nickName == null ? "空" : nickName) + 
				"，邮箱:" + (email == null ? "空" : email) + 
				"，qqOpenId:" + (qqOpenId == null ? "空" : qqOpenId) + 
				"，qq头像:" + (qqAvatar == null ? "空" : qqAvatar) + 
				"，密码:" + (password == null ? "空" : password) + 
				"，注册时间:" + (joinTime == null ? "空" : DateUtil.format(joinTime, DateTimePattenEnum.YYYY_MM_DD_HH_MM_SS.getPatten())) + 
				"，最后登录时间:" + (lastLoginTime == null ? "空" : DateUtil.format(lastLoginTime, DateTimePattenEnum.YYYY_MM_DD_HH_MM_SS.getPatten())) + 
				"，是否启用（0:禁用; 1:启用）:" + (status == null ? "空" : status) + 
				"，使用空间（单位：byte）:" + (useSpace == null ? "空" : useSpace) + 
				"，总空间（单位：byte）:" + (totalSpace == null ? "空" : totalSpace);
	}
}