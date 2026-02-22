package com.fmk.cource;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.fmk.cource.mapper")
public class CourseTargetApplication {
    public static void main(String[] args) {
        SpringApplication.run(CourseTargetApplication.class, args);

        // 使用 Java 文本块定义 FMK 图案，\033[36m 是开启青色字体，\033[0m 是恢复默认颜色
        String fmkLogo = """
                \033[36m
                 _______  __   __  ___   _  
                |       ||  |_|  ||   | | | 
                |    ___||       ||   |_| | 
                |   |___ |       ||      _| 
                |    ___||       ||     |_  
                |   |    | ||_|| ||    _  | 
                |___|    |_|   |_||___| |_| 
                \033[0m""";

        System.out.println(fmkLogo);
        // \033[32m 是开启绿色字体
        System.out.println("✨ \033[32m课程目标达成情况管理系统 启动成功!\033[0m ✨");
    }
}