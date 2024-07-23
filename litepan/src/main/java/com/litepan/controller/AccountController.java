package com.litepan.controller;

import com.litepan.annotation.GlobalInterceptor;
import com.litepan.annotation.VerifyParam;
import com.litepan.entity.constants.Constants;
import com.litepan.entity.dto.CreateImageCode;
import com.litepan.entity.po.EmailCode;
import com.litepan.enums.VerifyRegexEnum;
import com.litepan.exception.BusinessException;
import com.litepan.service.EmailCodeService;
import com.litepan.service.UserInfoService;
import com.litepan.entity.vo.ResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;


/**
 * @Description: 用户信息Controller
 * @date: 2024/07/09
 */
@RestController
//@RequestMapping("/userInfo")
@Slf4j
public class AccountController extends ABaseController {

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private EmailCodeService emailCodeService;

    /**
     * 根据请求类型返回验证码并存入session
     *
     * @param type 0:登录注册  1:邮箱验证码发送  默认0
     */
    @RequestMapping("/checkCode")
    @GlobalInterceptor(checkParams = true)
    public void checkCode(HttpServletResponse response, HttpSession session, Integer type)
            throws IOException {
        CreateImageCode vCode = new CreateImageCode(130, 38, 5, 10);
        response.setContentType("image/jpeg");
        response.setDateHeader("Expires", 0);
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        String code = vCode.getCode();
        if (type == null || type == 0) {
            session.setAttribute(Constants.CHECK_CODE_KEY, code);//type=0则表示普通验证码
        } else {
            session.setAttribute(Constants.CHECK_CODE_KEY_EMAIL, code);//type=1则表示发送邮件验证码
        }
        vCode.write(response.getOutputStream());
    }


    /**
     * 根据邮箱发送验证码
     *
     * @param session HttpSession
     * @param email 接收验证码的邮箱
     * @param checkCode 图片验证码
     * @param type 0：注册；1：找回密码；
     * @return 返回统一响应类
     */
    @RequestMapping("/sendEmailCode")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO<EmailCode> sendEmailCode(HttpSession session,
                                               @VerifyParam(required = true, max = 150, regex = VerifyRegexEnum.EMAIL) String email,
                                               @VerifyParam(required = true) String checkCode,
                                               @VerifyParam(required = true) Integer type) {
        try {
            if (!checkCode.equalsIgnoreCase((String) session.getAttribute(Constants.CHECK_CODE_KEY_EMAIL))) {// 校验邮件验证码
                throw new BusinessException("图片验证码错误");
            }
            emailCodeService.sendEmailCode(email, type);
            return getSuccessResponseVO(null);
        } finally {
            session.removeAttribute(Constants.CHECK_CODE_KEY_EMAIL);
        }
    }


}