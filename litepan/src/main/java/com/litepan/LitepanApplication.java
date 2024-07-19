package com.litepan;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(scanBasePackages = {"com.litepan"})
@EnableAsync
@EnableScheduling
@EnableTransactionManagement
@MapperScan("com.litepan.mappers")
public class LitepanApplication {

    public static void main(String[] args) {
        SpringApplication.run(LitepanApplication.class, args);
    }

}
