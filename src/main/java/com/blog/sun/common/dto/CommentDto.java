package com.blog.sun.common.dto;

import lombok.Data;

@Data
public class CommentDto {

    private Long userId;

    private String content;

    private Long blogId;

    private Long parentId;
}
