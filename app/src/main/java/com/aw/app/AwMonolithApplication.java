package com.aw.app;

import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.aw")
@MapperScan(basePackages = {"com.aw.auth.mapper", "com.aw.hr.mapper"})
@EnableProcessApplication
public class AwMonolithApplication {

    public static void main(String[] args) {
        SpringApplication.run(AwMonolithApplication.class, args);
    }
}