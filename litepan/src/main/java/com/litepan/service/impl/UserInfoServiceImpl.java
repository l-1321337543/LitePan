package com.litepan.service.impl;

import com.litepan.entity.config.AppConfig;
import com.litepan.entity.constants.Constants;
import com.litepan.entity.dto.SessionWebUserDTO;
import com.litepan.entity.dto.SysSettingDTO;
import com.litepan.entity.dto.UserSpaceDTO;
import com.litepan.entity.po.FileInfo;
import com.litepan.entity.query.FileInfoQuery;
import com.litepan.enums.UserStatusEnum;
import com.litepan.exception.BusinessException;
import com.litepan.mappers.FileInfoMapper;
import com.litepan.service.EmailCodeService;
import com.litepan.service.UserInfoService;
import com.litepan.entity.po.UserInfo;
import com.litepan.entity.query.UserInfoQuery;
import com.litepan.utils.RedisComponent;
import com.litepan.utils.StringTools;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

import com.litepan.entity.vo.PaginationResultVO;
import com.litepan.mappers.UserInfoMapper;
import com.litepan.entity.query.SimplePage;
import com.litepan.enums.PageSize;
import org.springframework.transaction.annotation.Transactional;


/**
 * @Description: 用户信息ServiceImpl
 * @date: 2024/07/09
 */
@Service("userInfoService")
public class UserInfoServiceImpl implements UserInfoService {

    @Resource
    private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

    @Resource
    private FileInfoMapper<FileInfo, FileInfoQuery> fileInfoMapper;

    @Resource
    private EmailCodeService emailCodeService;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private AppConfig appConfig;

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

    /**
     * 用户注册，并初始化可用空间
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(String email, String nickName, String password, String emailCode) {
        if (userInfoMapper.selectByEmail(email) != null) {
            throw new BusinessException("邮箱已存在");
        }
        if (userInfoMapper.selectByNickName(nickName) != null) {
            throw new BusinessException("昵称已存在");
        }
        emailCodeService.checkCode(email, emailCode);

        SysSettingDTO sysSettingDTO = redisComponent.getSysSettingDTO();

        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(StringTools.getRandomNumber(Constants.LENGTH_10));
        userInfo.setEmail(email);
        userInfo.setNickName(nickName);
        userInfo.setPassword(StringTools.encodeBuMd5(password));
        userInfo.setStatus(UserStatusEnum.ENABLE.getStatus());
        userInfo.setJoinTime(new Date());
        userInfo.setUseSpace(0L);
        userInfo.setTotalSpace(sysSettingDTO.getUserInitUseSpace() * Constants.MB);
        userInfoMapper.insert(userInfo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SessionWebUserDTO login(String email, String password) {
        UserInfo userInfo = userInfoMapper.selectByEmail(email);
        if (userInfo == null || !password.equals(userInfo.getPassword())) {
//        if (userInfo == null || !Objects.equals(StringUtils.encodeBuMd5(password), userInfo.getPassword())) {
            throw new BusinessException("邮箱或密码错误！");
        }

        if (userInfo.getStatus().equals(UserStatusEnum.DISABLE.getStatus())) {
            throw new BusinessException("该账户已被封禁");
        }
        UserInfo update = new UserInfo();
        update.setLastLoginTime(new Date());
        userInfoMapper.updateByEmail(update, email);

        SessionWebUserDTO sessionWebUserDto = new SessionWebUserDTO();
        sessionWebUserDto.setUserId(userInfo.getUserId());
        sessionWebUserDto.setNickName(userInfo.getNickName());
        sessionWebUserDto.setAdmin(ArrayUtils.contains(appConfig.getAdminEmails().split(","), email));

        // 用户空间
        UserSpaceDTO userSpaceDTO = new UserSpaceDTO();
        Long useSpace = fileInfoMapper.selectUseSpace(userInfo.getUserId());
        userSpaceDTO.setUseSpace(useSpace);
        userSpaceDTO.setTotalSpace(userInfo.getTotalSpace());
        redisComponent.saveUserSpaceUse(userInfo.getUserId(), userSpaceDTO);

        return sessionWebUserDto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPwd(String email, String password, String emailCode) {
        UserInfo userInfo = userInfoMapper.selectByEmail(email);
        if (userInfo == null) {
            throw new BusinessException("邮箱不存在");
        }
        emailCodeService.checkCode(email, emailCode);
        userInfo.setPassword(StringTools.encodeBuMd5(password));
        userInfoMapper.updateByEmail(userInfo, email);
    }
}