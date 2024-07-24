package com.litepan.controller;

import com.litepan.annotation.GlobalInterceptor;
import com.litepan.annotation.VerifyParam;
import com.litepan.entity.config.AppConfig;
import com.litepan.entity.constants.Constants;
import com.litepan.entity.dto.CreateImageCode;
import com.litepan.entity.dto.SessionWebUserDTO;
import com.litepan.enums.VerifyRegexEnum;
import com.litepan.exception.BusinessException;
import com.litepan.service.EmailCodeService;
import com.litepan.service.UserInfoService;
import com.litepan.entity.vo.ResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;


/**
 * @Description: 用户信息Controller
 * @date: 2024/07/09
 */
@RestController
//@RequestMapping("/userInfo")
@Slf4j
public class AccountController extends ABaseController {

    private static final String CONTENT_TYPE = "Content-Type";

    private static final String CONTENT_TYPE_VALUE = "application/json;charset=UTF-8";
    @Resource
    private UserInfoService userInfoService;

    @Resource
    private EmailCodeService emailCodeService;

    @Resource
    private AppConfig appConfig;

    /**
     * 根据请求类型返回验证码并存入session
     *
     * @param type 0:登录注册的图片验证码  1:发送邮箱验证码的图片验证码  默认0
     */
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
            session.setAttribute(Constants.CHECK_CODE_KEY, code);//type=0则表示登录注册的图片验证码
        } else {
            session.setAttribute(Constants.CHECK_CODE_KEY_EMAIL, code);//type=1则发送邮箱验证码的图片验证码
        }
        vCode.write(response.getOutputStream());
    }


    /**
     * 根据邮箱发送验证码
     *
     * @param session   HttpSession
     * @param email     接收验证码的邮箱
     * @param checkCode 图片验证码
     * @param type      0：注册；1：重置密码；
     * @return 返回统一响应类
     */
    @RequestMapping("/sendEmailCode")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO<Object> sendEmailCode(HttpSession session,
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

    /**
     * 用户注册
     */
    @RequestMapping("/register")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO<Object> register(HttpSession session,
                                       @VerifyParam(required = true, max = 150, regex = VerifyRegexEnum.EMAIL) String email,
                                       @VerifyParam(required = true) String emailCode,
                                       @VerifyParam(required = true) String nickName,
                                       @VerifyParam(required = true, min = 8, max = 18, regex = VerifyRegexEnum.PASSWORD) String password,
                                       @VerifyParam(required = true) String checkCode) {
        try {
            if (!checkCode.equalsIgnoreCase((String) session.getAttribute(Constants.CHECK_CODE_KEY))) {// 校验邮件验证码
                throw new BusinessException("图片验证码错误");
            }
            userInfoService.register(email, nickName, password, emailCode);
            return getSuccessResponseVO(null);
        } finally {
            session.removeAttribute(Constants.CHECK_CODE_KEY);
        }
    }

    /**
     * 用户登录
     */
    @RequestMapping("/login")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO<SessionWebUserDTO> login(HttpSession session,
                                               @VerifyParam(required = true) String email,
                                               @VerifyParam(required = true) String password,
                                               String checkCode) {
        try {
            if (!checkCode.equalsIgnoreCase((String) session.getAttribute(Constants.CHECK_CODE_KEY))) {// 校验邮件验证码
                throw new BusinessException("图片验证码错误");
            }
            SessionWebUserDTO sessionWebUserDto = userInfoService.login(email, password);
            session.setAttribute(Constants.SESSION_KEY, sessionWebUserDto);
            return getSuccessResponseVO(sessionWebUserDto);
        } finally {
            session.removeAttribute(Constants.CHECK_CODE_KEY);
        }
    }

    /**
     * 重置密码
     */
    @RequestMapping("/resetPwd")
    public ResponseVO<Object> resetPwd(HttpSession session,
                                       @VerifyParam(required = true, max = 150, regex = VerifyRegexEnum.EMAIL) String email,
                                       @VerifyParam(required = true, min = 8, max = 18, regex = VerifyRegexEnum.PASSWORD) String password,
                                       @VerifyParam(required = true) String checkCode,
                                       @VerifyParam(required = true) String emailCode) {
        try {
            if (!checkCode.equalsIgnoreCase((String) session.getAttribute(Constants.CHECK_CODE_KEY))) {// 校验邮件验证码
                throw new BusinessException("图片验证码错误");
            }
            userInfoService.resetPwd(email, password, emailCode);
            return getSuccessResponseVO(null);
        } finally {
            session.removeAttribute(Constants.CHECK_CODE_KEY);
        }

    }

    /**
     * 获取用户头像，没有则提供默认头像
     */
    @RequestMapping("getAvatar/{userId}")
    public void getAvatar(HttpServletResponse response,
                          @VerifyParam(required = true) @PathVariable String userId) {
        //用户头像文件夹路径
        String avatarFolderPath = Constants.FILE_FOLDER_FILE + Constants.FILE_FOLDER_AVATAR_NAME;
        //用户头像文件路径
        String avatarPath = appConfig.getProjectFolder() + avatarFolderPath + userId + Constants.AVATAR_SUFFIX;
        //默认头像文件路径
        String avatarDefaultPath = appConfig.getProjectFolder() + avatarFolderPath + Constants.AVATAR_DEFAULT;
        File folder = new File(appConfig.getProjectFolder() + avatarFolderPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File avatarFile = new File(avatarPath);
        if (!avatarFile.exists()) {
            if (!new File(avatarDefaultPath).exists()) {
                printNoDefaultImage(response);
            }
            avatarPath = avatarDefaultPath;
        }
        response.setContentType("image/jpg");
        readFile(response, avatarPath);

    }

    /**
     * 没有放置默认头像，执行该方法
     */
    private void printNoDefaultImage(HttpServletResponse response) {
        response.setHeader(CONTENT_TYPE, CONTENT_TYPE_VALUE);
        response.setStatus(HttpStatus.OK.value());
        try (PrintWriter writer = response.getWriter()) {
            writer.print("请在头像目录下放置默认头像default_avatar.jpg");
        } catch (Exception e) {
            log.error("输出无默认图失败", e);
        }
    }

}