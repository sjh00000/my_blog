package com.blog.sun;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
@MapperScan("com.blog.sun.mapper")
public class VueblogApplication {
    public static void main(String[] args) {
        SpringApplication.run(VueblogApplication.class, args);
    }

}