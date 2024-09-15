package com.blog.sun.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.sun.common.dao.BlogDao;
import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.sun.common.dto.BlogDto;
import com.blog.sun.common.vo.BlogVo;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author sjh
 * @since 2024-05-16
 */
public interface BlogService extends IService<BlogDao> {

    IPage<BlogVo> getBlogList(Page<BlogVo> page);

    BlogVo getBlogById(Long blogId);

    Boolean editBlogDetail(BlogDto blogDto);

    IPage<BlogVo> searchBlogsLikeTitleOrContent(Page<BlogVo> page, String keyword);

    IPage<BlogVo> queryByLabel(Page<BlogVo> page, String label);

    List<Object> queryAllTags();
}
