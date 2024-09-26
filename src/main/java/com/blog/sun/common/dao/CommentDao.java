package com.blog.sun.common.dao;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("comments")
public class CommentDao {

    private Long id;

    @NotBlank(message = "内容不能为空")
    private String content;

    private Long userId;

    private Long blogId;

    private Integer parentId;

    @JsonFormat(pattern="yyyy-MM-dd")
    private Date createdAt;

}
