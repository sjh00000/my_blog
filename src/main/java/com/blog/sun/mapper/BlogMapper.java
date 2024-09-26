package com.blog.sun.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.sun.common.dao.BlogDao;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.sun.common.vo.BlogVo;
import com.blog.sun.common.vo.CommentVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author sjh
 * @since 2024-05-16
 */
public interface BlogMapper extends BaseMapper<BlogDao> {

    IPage<BlogDao> getBlogList(@Param("page") Page<BlogVo> page);

    BlogDao getBlogById(@Param("blogId") Long blogId);

    void saveBlog(@Param("blogDao") BlogDao blogDao);

    void updateBlog(@Param("blogDao") BlogDao blogDao);

    IPage<BlogDao> searchBlogsLikeTitleOrDescription(@Param("page") Page<BlogVo> page, @Param("keyword") String keyword);

    IPage<BlogDao> queryByLabel(@Param("page") Page<BlogVo> page, @Param("label") String label);

    List<String> queryAllTags();

    List<CommentVo> getCommentList(@Param("blogId") Long blogId, @Param("parentId") Long parentId);

    void editComment(@Param("blogId") Long blogId, @Param("content") String content, @Param("parentId") Long parentId, @Param("userId") Long userId);
}
