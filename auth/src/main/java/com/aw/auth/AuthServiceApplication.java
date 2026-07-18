package com.aw.auth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "com.aw.auth",
        "com.aw.common.security"  // Ép Spring quét thêm các bean bảo mật dùng chung
})
//@SpringBootApplication
@MapperScan("com.aw.auth.mapper")
public class AuthServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}