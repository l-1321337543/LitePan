package com.litepan.service.impl;

import com.litepan.service.UserInfoService;
import com.litepan.entity.po.UserInfo;
import com.litepan.entity.query.UserInfoQuery;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import com.litepan.entity.vo.PaginationResultVO;
import com.litepan.mappers.UserInfoMapper;
import com.litepan.entity.query.SimplePage;
import com.litepan.enums.PageSize;


/**
 * @Description: 用户信息ServiceImpl
 * @date: 2024/07/09
 */
@Service("userInfoService")
public class UserInfoServiceImpl implements UserInfoService {

	@Resource
	private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<UserInfo> findListByParam(UserInfoQuery query) {
		return userInfoMapper.selectListByQuery(query);
	}

	/**
	 * 根据条件查询数量
	 */
	@Override
	public Integer findCountByParam(UserInfoQuery query) {
		return userInfoMapper.selectCountByQuery(query);
	}

	/**
	 * 分页查询
	 */
	@Override
	public PaginationResultVO<UserInfo> findListByPage(UserInfoQuery query) {
		Integer count = this.findCountByParam(query);
		int pageSize = query.getPageSize() == null ? PageSize.SIZE15.getSize() : query.getPageSize();
		SimplePage page = new SimplePage(query.getPageNo(), count, pageSize);
		query.setSimplePage(page);
		return new PaginationResultVO<>(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), this.findListByParam(query));
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(UserInfo bean) {
		return userInfoMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<UserInfo> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return userInfoMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增/修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<UserInfo> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return userInfoMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 根据userId查询
	 */
	@Override
	public UserInfo getUserInfoByUserId(String userId) {
		return userInfoMapper.selectByUserId(userId);
	}

	/**
	 * 根据userId更新
	 */
	@Override
	public Integer updateUserInfoByUserId(UserInfo bean, String userId) {
		return userInfoMapper.updateByUserId(bean, userId);
	}

	/**
	 * 根据userId删除
	 */
	@Override
	public Integer deleteUserInfoByUserId(String userId) {
		return userInfoMapper.deleteByUserId(userId);
	}

	/**
	 * 根据email查询
	 */
	@Override
	public UserInfo getUserInfoByEmail(String email) {
		return userInfoMapper.selectByEmail(email);
	}

	/**
	 * 根据email更新
	 */
	@Override
	public Integer updateUserInfoByEmail(UserInfo bean, String email) {
		return userInfoMapper.updateByEmail(bean, email);
	}

	/**
	 * 根据email删除
	 */
	@Override
	public Integer deleteUserInfoByEmail(String email) {
		return userInfoMapper.deleteByEmail(email);
	}

	/**
	 * 根据qqOpenId查询
	 */
	@Override
	public UserInfo getUserInfoByQqOpenId(String qqOpenId) {
		return userInfoMapper.selectByQqOpenId(qqOpenId);
	}

	/**
	 * 根据qqOpenId更新
	 */
	@Override
	public Integer updateUserInfoByQqOpenId(UserInfo bean, String qqOpenId) {
		return userInfoMapper.updateByQqOpenId(bean, qqOpenId);
	}

	/**
	 * 根据qqOpenId删除
	 */
	@Override
	public Integer deleteUserInfoByQqOpenId(String qqOpenId) {
		return userInfoMapper.deleteByQqOpenId(qqOpenId);
	}

	/**
	 * 根据nickName查询
	 */
	@Override
	public UserInfo getUserInfoByNickName(String nickName) {
		return userInfoMapper.selectByNickName(nickName);
	}

	/**
	 * 根据nickName更新
	 */
	@Override
	public Integer updateUserInfoByNickName(UserInfo bean, String nickName) {
		return userInfoMapper.updateByNickName(bean, nickName);
	}

	/**
	 * 根据nickName删除
	 */
	@Override
	public Integer deleteUserInfoByNickName(String nickName) {
		return userInfoMapper.deleteByNickName(nickName);
	}

}