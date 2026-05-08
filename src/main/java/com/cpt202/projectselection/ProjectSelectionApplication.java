package com.cpt202.projectselection;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@MapperScan("com.cpt202.projectselection.mapper")
@EnableAsync
public class ProjectSelectionApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjectSelectionApplication.class, args);
    }
}
