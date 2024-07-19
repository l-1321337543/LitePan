package com.litepan.entity.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @description: 读取配置文件中的值
 * @author 李臣洋
 * @date 2024/7/19 9:33
 * @version 1.0
 */
@Component
public class AppConfig {
    @Value("${spring.mail.username}")
    private String fromEmail;

    public String getFromEmail() {
        return fromEmail;
    }
}
