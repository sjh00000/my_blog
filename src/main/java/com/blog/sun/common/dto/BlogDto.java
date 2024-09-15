package com.blog.sun.common.dto;

import lombok.Data;

import java.util.Date;

@Data
public class BlogDto {
    private Long id;

    private Long userId;

    private String title;

    private String description;

    private String content;

    private Date created;

    private Integer status;

    private String label;
}
