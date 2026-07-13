package com.aw.hr;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "com.aw.hr",
        "com.aw.common.security"  // Ép Spring quét thêm các bean bảo mật dùng chung
})
@MapperScan("com.aw.hr.mapper")
public class HrApplication {
    public static void main(String[] args) {
        SpringApplication.run(HrApplication.class, args);
    }
}
