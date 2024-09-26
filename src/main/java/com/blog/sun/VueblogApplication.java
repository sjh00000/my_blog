package com.blog.sun;
import org.apache.shiro.spring.boot.autoconfigure.ShiroAnnotationProcessorAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication(exclude = {ShiroAnnotationProcessorAutoConfiguration.class})
@MapperScan("com.blog.sun.mapper")

public class VueblogApplication {
    public static void main(String[] args) {
        SpringApplication.run(VueblogApplication.class, args);
    }

}