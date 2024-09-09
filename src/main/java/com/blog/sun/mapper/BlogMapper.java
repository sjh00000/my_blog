package com.blog.sun.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.sun.common.dao.BlogDao;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.sun.common.vo.BlogVo;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author sjh
 * @since 2024-05-16
 */
public interface BlogMapper extends BaseMapper<BlogDao> {

    IPage<BlogDao> getBlogList(Page<BlogVo> page);

    BlogDao getBlogById(Long blogId);

    void saveOrUpdateBlog(BlogDao blogDao);

    IPage<BlogDao> searchBlogsLikeTitleOrDescription(Page<BlogVo> page, String keyword);

    IPage<BlogDao> queryByLabel(Page<BlogVo> page, String label);

    List<Object> queryAllTags();
}
