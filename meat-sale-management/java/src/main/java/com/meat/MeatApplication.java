package com.meat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MeatApplication {

    public static void main(String[] args) {
        SpringApplication.run(MeatApplication.class, args);
        System.out.println("========================================");
        System.out.println("  肉类销售管理系统启动成功！");
        System.out.println("  Knife4j API文档: http://localhost:8080/doc.html");
        System.out.println("========================================");

    }
}
