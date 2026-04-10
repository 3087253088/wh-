package com.wanghao.smartfinancialintegrationplatform;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.wanghao.*")  // 添加这行，扫描整个 com.wanghao 包
@MapperScan("com.wanghao.modules")
public class SmartFinancialIntegrationPlatformApplication {
    public static void main(String[] args) {
        SpringApplication.run(SmartFinancialIntegrationPlatformApplication.class, args);
    }
}