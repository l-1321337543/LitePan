package com.litepan.controller;

import com.litepan.entity.constants.Constants;
import com.litepan.entity.dto.CreateImageCode;
import com.litepan.entity.po.EmailCode;
import com.litepan.exception.BusinessException;
import com.litepan.service.EmailCodeService;
import com.litepan.service.UserInfoService;
import com.litepan.entity.vo.ResponseVO;
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
public class AccountController extends ABaseController {

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private EmailCodeService emailCodeService;

    @RequestMapping("/checkCode")
    public void checkCode(HttpServletResponse response, HttpSession session, Integer type)
            throws IOException {
        CreateImageCode vCode = new CreateImageCode(130, 38, 5, 10);
        response.setContentType("image/jpeg");
        response.setDateHeader("Expires", 0);
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        String code = vCode.getCode();
        if (type == null || type == 0) {
            session.setAttribute(Constants.CHECK_CODE_KEY, code);
        } else {
            session.setAttribute(Constants.CHECK_CODE_KEY_EMAIL, code);
        }
        vCode.write(response.getOutputStream());
    }

    @RequestMapping("/sendEmailCode")
    public ResponseVO<EmailCode> sendEmailCode(HttpSession session, String email, String checkCode, Integer type) {
        try {
            if (!checkCode.equalsIgnoreCase((String) session.getAttribute(Constants.CHECK_CODE_KEY_EMAIL))) {
                throw new BusinessException("图片验证码错误");
            }
            emailCodeService.sendEmailCode(email, type);
            return getSuccessResponseVO(null);
        } finally {
            session.removeAttribute(Constants.CHECK_CODE_KEY_EMAIL);
        }
    }


}