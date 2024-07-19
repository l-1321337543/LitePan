package com.litepan.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author 李臣洋
 * @version 1.0
 * @description: 默认的邮箱内容设置
 * @date 2024/7/19 10:15
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SysSettingDto {

    /**
     * 注册发送邮件标题
     */
    private String registerEmailTitle = "邮箱验证码";

    /**
     * 注册发送邮件内容
     */
    private String registerEmailContent = "您好，您的验证码是%s，15分钟有效";


    /**
     * 用户初始化空间大小 5M
     */
    private Integer userInitUseSpace = 5;

    public String getRegisterEmailTitle() {
        return registerEmailTitle;
    }

    public void setRegisterEmailTitle(String registerEmailTitle) {
        this.registerEmailTitle = registerEmailTitle;
    }

    public String getRegisterEmailContent() {
        return registerEmailContent;
    }

    public void setRegisterEmailContent(String registerEmailContent) {
        this.registerEmailContent = registerEmailContent;
    }

    public Integer getUserInitUseSpace() {
        return userInitUseSpace;
    }

    public void setUserInitUseSpace(Integer userInitUseSpace) {
        this.userInitUseSpace = userInitUseSpace;
    }
}
