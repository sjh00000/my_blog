package com.blog.sun.common.vo;

import lombok.Data;

import java.util.Date;

@Data
public class BlogVo {
    private Long id;

    private Long userId;

    private String title;

    private String description;

    private String content;

    private Date created;

    private Integer status;

    private String label;
}
