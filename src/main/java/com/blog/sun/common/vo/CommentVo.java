package com.blog.sun.common.vo;

import lombok.Data;

@Data
public class CommentVo {

    private String username;

    private String avatar;

    private String content;

    private String createdAt;

    private Long parentId;
}
