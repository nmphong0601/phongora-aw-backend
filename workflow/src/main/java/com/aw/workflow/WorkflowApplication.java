package com.aw.workflow;

import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "com.aw.workflow",
        "com.aw.common.security"  // Ép Spring quét thêm các bean bảo mật dùng chung
})
@EnableProcessApplication
public class WorkflowApplication {
    public static void main(String[] args) {
        SpringApplication.run(WorkflowApplication.class, args);
    }
}