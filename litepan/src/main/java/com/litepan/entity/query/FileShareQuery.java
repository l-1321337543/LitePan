package com.litepan.entity.query;

import java.util.Date;

/**
 * @Description: 分享信息表查询对象
 * @date: 2024/08/01
 */
public class FileShareQuery extends BaseQuery {

	/**
	 * 分享id
	 */
	private String shareId;

	private String shareIdFuzzy;



	/**
	 * 文件id
	 */
	private String fileId;

	private String fileIdFuzzy;



	/**
	 * 分享人id
	 */
	private String userId;

	private String userIdFuzzy;



	/**
	 * 有效期(0:1天;1:7天;2:30天;3:永久)
	 */
	private Integer validType;



	/**
	 * 失效时间
	 */
	private Date expireTime;

	private String expireTimeStart;

	private String expireTimeEnd;



	/**
	 * 分享时间
	 */
	private Date shareTime;

	private String shareTimeStart;

	private String shareTimeEnd;



	/**
	 * 提取码
	 */
	private String code;

	private String codeFuzzy;



	/**
	 * 浏览次数
	 */
	private Integer showCount;


	private Boolean queryFileName;

	public Boolean getQueryFileName() {
		return queryFileName;
	}

	public void setQueryFileName(Boolean queryFileName) {
		this.queryFileName = queryFileName;
	}

	public void setShareId(String shareId) {
		this.shareId = shareId;
	}

	public String getShareId() {
		return shareId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	public String getFileId() {
		return fileId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserId() {
		return userId;
	}

	public void setValidType(Integer validType) {
		this.validType = validType;
	}

	public Integer getValidType() {
		return validType;
	}

	public void setExpireTime(Date expireTime) {
		this.expireTime = expireTime;
	}

	public Date getExpireTime() {
		return expireTime;
	}

	public void setShareTime(Date shareTime) {
		this.shareTime = shareTime;
	}

	public Date getShareTime() {
		return shareTime;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public void setShowCount(Integer showCount) {
		this.showCount = showCount;
	}

	public Integer getShowCount() {
		return showCount;
	}

	public void setShareIdFuzzy(String shareIdFuzzy) {
		this.shareIdFuzzy = shareIdFuzzy;
	}

	public String getShareIdFuzzy() {
		return shareIdFuzzy;
	}

	public void setFileIdFuzzy(String fileIdFuzzy) {
		this.fileIdFuzzy = fileIdFuzzy;
	}

	public String getFileIdFuzzy() {
		return fileIdFuzzy;
	}

	public void setUserIdFuzzy(String userIdFuzzy) {
		this.userIdFuzzy = userIdFuzzy;
	}

	public String getUserIdFuzzy() {
		return userIdFuzzy;
	}

	public void setExpireTimeStart(String expireTimeStart) {
		this.expireTimeStart = expireTimeStart;
	}

	public String getExpireTimeStart() {
		return expireTimeStart;
	}

	public void setExpireTimeEnd(String expireTimeEnd) {
		this.expireTimeEnd = expireTimeEnd;
	}

	public String getExpireTimeEnd() {
		return expireTimeEnd;
	}

	public void setShareTimeStart(String shareTimeStart) {
		this.shareTimeStart = shareTimeStart;
	}

	public String getShareTimeStart() {
		return shareTimeStart;
	}

	public void setShareTimeEnd(String shareTimeEnd) {
		this.shareTimeEnd = shareTimeEnd;
	}

	public String getShareTimeEnd() {
		return shareTimeEnd;
	}

	public void setCodeFuzzy(String codeFuzzy) {
		this.codeFuzzy = codeFuzzy;
	}

	public String getCodeFuzzy() {
		return codeFuzzy;
	}

}