package com.blog.sun.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.sun.common.dao.BlogDao;
import com.blog.sun.common.dto.BlogDto;
import com.blog.sun.common.vo.BlogVo;
import com.blog.sun.mapper.BlogMapper;
import com.blog.sun.service.BlogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.sun.util.ShiroUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author sjh
 * @since 2024-05-16
 */
@Service
@Slf4j
public class BlogServiceImpl extends ServiceImpl<BlogMapper, BlogDao> implements BlogService {

    @Autowired
    private BlogMapper blogMapper;
    @Override
    public IPage<BlogVo> getBlogList(Page<BlogVo> page) {
        IPage<BlogDao> blogDaoIPage = blogMapper.getBlogList(page);
        IPage<BlogVo> blogVoIPage = new Page<>();
        BeanUtils.copyProperties(blogDaoIPage,blogVoIPage);
        return blogVoIPage;
    }

    @Override
    public BlogVo getBlogById(Long blogId) {
        BlogDao blogDao =  blogMapper.getBlogById(blogId);
        BlogVo blogVo = new BlogVo();
        BeanUtils.copyProperties(blogDao,blogVo);
        return blogVo;
    }

    @Override
    public void editBlogDetail(BlogDto blogDto) {
        BlogDao blogDao;
        // 根据博客ID判断是更新现有博客还是创建新博客
        if(blogDto.getId() != null) {
            blogDao = blogMapper.getBlogById(blogDto.getId());
            //查询用户是否是博客作者
            log.info("博客已存在，作者是：{}", blogDao.getUserId());
            Assert.isTrue(blogDao.getUserId().longValue() == ShiroUtil.getProfile().getId().longValue(), "没有权限编辑");
        } else {
            blogDao = new BlogDao();
            blogDao.setUserId(ShiroUtil.getProfile().getId());
            blogDao.setCreated(LocalDateTime.now());
            log.info("新增博客");
        }

        // 复制博客属性到临时对象，忽略特定属性以防止覆盖
        BeanUtil.copyProperties(blogDto, blogDao, "id", "userId", "created");
        blogMapper.saveOrUpdateBlog(blogDao);
    }

    @Override
    public IPage<BlogVo> searchBlogsLikeTitleOrContent(Page<BlogVo> page, String keyword) {
        IPage<BlogDao> blogDaoIPage = blogMapper.searchBlogsLikeTitleOrDescription(page,keyword);
        IPage<BlogVo> blogVoIPage = new Page<>();
        BeanUtils.copyProperties(blogDaoIPage,blogVoIPage);
        return blogVoIPage;
    }

    @Override
    public IPage<BlogVo> queryByLabel(Page<BlogVo> page, String label) {
        IPage<BlogDao> blogDaoIPage =blogMapper.queryByLabel(page, label);
        IPage<BlogVo> blogVoIPage = new Page<>();
        BeanUtils.copyProperties(blogDaoIPage,blogVoIPage);
        return blogVoIPage;
    }

    @Override
    public List<Object> queryAllTags() {
        return blogMapper.queryAllTags();
    }
}
