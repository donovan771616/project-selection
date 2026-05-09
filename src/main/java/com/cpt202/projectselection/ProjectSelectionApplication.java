package com.cpt202.projectselection;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.cpt202.projectselection.mapper")
public class ProjectSelectionApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjectSelectionApplication.class, args);
    }
}
