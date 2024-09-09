package com.blog.sun.common.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserVo {
    private Long id;
    private String username;
    private String email;
    private String avatar;
    private Integer status;
    private LocalDateTime created;
}
