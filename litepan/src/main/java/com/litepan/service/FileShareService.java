package com.litepan.service;

import com.litepan.entity.po.FileShare;
import com.litepan.entity.query.FileShareQuery;
import java.util.List;
import com.litepan.entity.vo.PaginationResultVO;


/**
 * @Description: 分享信息表Service
 * @date: 2024/08/01
 */
public interface FileShareService {

	/**
	 * 根据条件查询列表
	 */
	List<FileShare> findListByParam(FileShareQuery query);

	/**
	 * 根据条件查询数量
	 */
	Integer findCountByParam(FileShareQuery query);

	/**
	 * 分页查询
	 */
	PaginationResultVO<FileShare> findListByPage(FileShareQuery query);

	/**
	 * 新增
	 */
	Integer add(FileShare bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<FileShare> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<FileShare> listBean);

	/**
	 * 根据shareId查询
	 */
	FileShare getFileShareByShareId(String shareId);

	/**
	 * 根据shareId更新
	 */
	Integer updateFileShareByShareId(FileShare bean, String shareId);

	/**
	 * 根据shareId删除
	 */
	Integer deleteFileShareByShareId(String shareId);

	void saveShare(FileShare fileShare);

	void delFileShareBatch(String userId,String shareIds);
}