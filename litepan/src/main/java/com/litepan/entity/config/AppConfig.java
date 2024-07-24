package com.litepan.entity.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author 李臣洋
 * @version 1.0
 * @description: 读取配置文件中的值
 * @date 2024/7/19 9:33
 */
@Component
public class AppConfig {
    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${admin.emails}")
    private String adminEmails;

    @Value("${project.folder}")
    private String projectFolder;

    public String getProjectFolder() {
        return projectFolder;
    }

    public String getAdminEmails() {
        return adminEmails;
    }

    public String getFromEmail() {
        return fromEmail;
    }
}
