package com.litepan.service.impl;

import com.litepan.utils.RedisComponent;
import com.litepan.entity.config.AppConfig;
import com.litepan.entity.constants.Constants;
import com.litepan.entity.dto.SysSettingDTO;
import com.litepan.entity.po.UserInfo;
import com.litepan.entity.query.UserInfoQuery;
import com.litepan.exception.BusinessException;
import com.litepan.mappers.UserInfoMapper;
import com.litepan.service.EmailCodeService;
import com.litepan.entity.po.EmailCode;
import com.litepan.entity.query.EmailCodeQuery;
import com.litepan.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.mail.internet.MimeMessage;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import com.litepan.entity.vo.PaginationResultVO;
import com.litepan.mappers.EmailCodeMapper;
import com.litepan.entity.query.SimplePage;
import com.litepan.enums.PageSize;
import org.springframework.transaction.annotation.Transactional;


/**
 * @Description: 邮箱验证码ServiceImpl
 * @date: 2024/07/18
 */
@Service("emailCodeService")
@Slf4j
public class EmailCodeServiceImpl implements EmailCodeService {

    @Resource
    private EmailCodeMapper<EmailCode, EmailCodeQuery> emailCodeMapper;

    @Resource
    private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

    @Resource
    private JavaMailSender javaMailSender;

    @Resource
    private AppConfig appConfig;

    @Resource
    private RedisComponent redisComponent;

    /**
     * 根据条件查询列表
     */
    @Override
    public List<EmailCode> findListByParam(EmailCodeQuery query) {
        return emailCodeMapper.selectListByQuery(query);
    }

    /**
     * 根据条件查询数量
     */
    @Override
    public Integer findCountByParam(EmailCodeQuery query) {
        return emailCodeMapper.selectCountByQuery(query);
    }

    /**
     * 分页查询
     */
    @Override
    public PaginationResultVO<EmailCode> findListByPage(EmailCodeQuery query) {
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
    public Integer add(EmailCode bean) {
        return emailCodeMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<EmailCode> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return emailCodeMapper.insertBatch(listBean);
    }

    /**
     * 批量新增/修改
     */
    @Override
    public Integer addOrUpdateBatch(List<EmailCode> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return emailCodeMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 根据emailAndCode查询
     */
    @Override
    public EmailCode getEmailCodeByEmailAndCode(String email, String code) {
        return emailCodeMapper.selectByEmailAndCode(email, code);
    }

    /**
     * 根据emailAndCode更新
     */
    @Override
    public Integer updateEmailCodeByEmailAndCode(EmailCode bean, String email, String code) {
        return emailCodeMapper.updateByEmailAndCode(bean, email, code);
    }

    /**
     * 根据emailAndCode删除
     */
    @Override
    public Integer deleteEmailCodeByEmailAndCode(String email, String code) {
        return emailCodeMapper.deleteByEmailAndCode(email, code);
    }

    /**
     * 发送邮箱验证码
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendEmailCode(String email, Integer type) {
        if (type == Constants.ZERO) { //注册
            UserInfo user = userInfoMapper.selectByEmail(email);
            if (user != null) {
                throw new BusinessException("邮箱已存在");
            }
            String code = StringUtils.getRandomNumber(Constants.LENGTH_5);

            //发送验证码
            sendEmailCode(email, code);

            emailCodeMapper.disableEmailCode(email);//将已发送验证码置为无效
            EmailCode emailCode = new EmailCode();
            emailCode.setCode(code);
            emailCode.setEmail(email);
            emailCode.setCreateTime(new Date());
            emailCode.setStatus(Constants.ZERO);
            emailCodeMapper.insert(emailCode);
        }
    }

    /**
     * 校验邮箱验证码
     */
    @Override
    public void checkCode(String email, String code) {
        EmailCode emailCode = emailCodeMapper.selectByEmailAndCode(email, code);
        if (emailCode == null) {
            throw new BusinessException("邮箱或验证码错误");
        }
        if (emailCode.getStatus() == Constants.ONE) {
            throw new BusinessException("验证码已被使用");
        }
        if (Instant.now().toEpochMilli() - emailCode.getCreateTime().getTime() > Constants.LENGTH_15 * 60 * 1000) {
            throw new BusinessException("验证码已超时");
        }
        emailCodeMapper.disableEmailCode(email);
    }

    private void sendEmailCode(String toEmail, String code) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(toEmail);
            helper.setFrom(appConfig.getFromEmail());
            SysSettingDTO sysSettingDTO = redisComponent.getSysSettingDTO();
            helper.setSubject(sysSettingDTO.getRegisterEmailTitle());
            helper.setText(String.format(sysSettingDTO.getRegisterEmailContent(), code));
            helper.setSentDate(new Date());
            javaMailSender.send(message);
        } catch (Exception e) {
            log.error("邮件发送出错", e);
            throw new BusinessException("邮件发送出错");
        }
    }

}