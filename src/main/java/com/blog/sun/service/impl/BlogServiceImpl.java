package com.blog.sun.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.sun.common.dao.BlogDao;
import com.blog.sun.common.dto.BlogDto;
import com.blog.sun.common.vo.BlogVo;
import com.blog.sun.mapper.BlogMapper;
import com.blog.sun.service.BlogService;
import com.blog.sun.util.ShiroUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 服务实现类
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

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final String cacheBlogInfoKey = "blog:info";

    private final String cacheBlogListKey = "blog:list";

    @Override
    public IPage<BlogVo> getBlogList(Page<BlogVo> page) {
        // 尝试从 Redis 缓存中获取数据
        long start = (page.getCurrent() - 1) * page.getSize();
        long end = start + page.getSize() - 1;

        List<Object> cacheBlogList = redisTemplate.opsForList().range(cacheBlogListKey, start, end);

        if (!CollectionUtils.isEmpty(cacheBlogList)) {
            // 如果缓存中有数据，则转换为 IPage<BlogDao> 类型
            log.info("Redis中找到博客列表");
            IPage<Object> cachedBlogPage = convertListToPage(cacheBlogList, page);
            IPage<BlogVo> blogVoIPage = new Page<>();
            BeanUtils.copyProperties(cachedBlogPage, blogVoIPage);
            return blogVoIPage;
        } else {
            // 如果缓存中没有数据，则从数据库查询数据
            log.info("Redis中未找到博客列表");
            IPage<BlogDao> blogDaoIPage = blogMapper.getBlogList(page);
            IPage<BlogVo> blogVoIPage = new Page<>();
            BeanUtils.copyProperties(blogDaoIPage, blogVoIPage);

            // 将查询结果保存到缓存中
            log.info("当前记录为{}", blogDaoIPage.getRecords());
            saveBlogListToCache(blogDaoIPage.getRecords());

            return blogVoIPage;
        }
    }

    @Override
    public BlogVo getBlogById(Long blogId) {
        // 尝试从 Redis 缓存中获取博客信息
        Object cachedBlog = redisTemplate.opsForHash().get(cacheBlogInfoKey, blogId);

        if (cachedBlog != null) {
            // 如果缓存中有数据，则直接返回转换后的 BlogVo
            log.info("Redis找到博客数据");
            BlogVo blogVo = new BlogVo();
            BeanUtils.copyProperties(cachedBlog, blogVo);
            return blogVo;
        } else {
            // 如果缓存中没有数据，则从数据库查询数据
            log.info("Redis未找到博客数据");
            BlogDao blogDao = blogMapper.getBlogById(blogId);
            BlogVo blogVo = new BlogVo();
            BeanUtils.copyProperties(blogDao, blogVo);

            // 将查询结果保存到缓存中
            saveBlogInfoToCache(blogDao);

            return blogVo;
        }
    }

    @Override
    public Boolean editBlogDetail(BlogDto blogDto) {
        BlogDao blogDao;
        // 根据博客ID判断是更新现有博客还是创建新博客
        if (blogDto.getId() != null) {
            blogDao = (BlogDao)redisTemplate.opsForHash().get(cacheBlogInfoKey, blogDto.getId());
            if (blogDao == null) {
                log.info("Redis未找到对应的博客");
                blogDao = blogMapper.getBlogById(blogDto.getId());
            }else{
                log.info("Redis中找到对应的博客");
                //先删除缓存
                deleteBlogFromCache(blogDao.getId());
            }
            //查询用户是否是博客作者
            if (blogDao != null) {
                log.info("博客已存在，作者是：{}", blogDao.getUserId());
                Assert.isTrue(blogDao.getUserId().longValue() == ShiroUtil.getProfile().getId().longValue(), "没有权限编辑");
                BeanUtil.copyProperties(blogDto, blogDao, "id", "userId", "created");
                //修改数据库
                blogMapper.updateBlog(blogDao);
                return true;
            } else {
                log.info("未找到对应id的博客");
                return false;
            }
        } else {
            blogDao = new BlogDao();
            blogDao.setUserId(ShiroUtil.getProfile().getId());
            blogDao.setCreated(Date.from(Instant.now()));
            log.info("新增博客");
            // 复制博客属性到临时对象，忽略特定属性以防止覆盖
            BeanUtil.copyProperties(blogDto, blogDao, "id", "userId", "created");
            //先保存到数据库再修改缓存
            blogMapper.saveBlog(blogDao);
            pushBlogToRedis(blogDao);
            return true;
        }

    }


    @Override
    public IPage<BlogVo> searchBlogsLikeTitleOrContent(Page<BlogVo> page, String keyword) {
        IPage<BlogDao> blogDaoIPage = blogMapper.searchBlogsLikeTitleOrDescription(page, keyword);
        IPage<BlogVo> blogVoIPage = new Page<>();
        BeanUtils.copyProperties(blogDaoIPage, blogVoIPage);
        return blogVoIPage;
    }

    @Override
    public IPage<BlogVo> queryByLabel(Page<BlogVo> page, String label) {
        IPage<BlogDao> blogDaoIPage = blogMapper.queryByLabel(page, label);
        IPage<BlogVo> blogVoIPage = new Page<>();
        BeanUtils.copyProperties(blogDaoIPage, blogVoIPage);
        return blogVoIPage;
    }

    @Override
    public List<Object> queryAllTags() {
        return blogMapper.queryAllTags();
    }

    private IPage<Object> convertListToPage(List<Object> list, Page<BlogVo> page) {
        IPage<Object> cachePage = new Page<>(page.getCurrent(), page.getSize());
        cachePage.setRecords(list);
        return cachePage;
    }

    private void saveBlogListToCache(List<BlogDao> records) {
        redisTemplate.opsForList().rightPushAll(cacheBlogListKey, records);
    }

    private void saveBlogInfoToCache(BlogDao blogDao) {
        redisTemplate.opsForHash().put(cacheBlogInfoKey, blogDao.getId(), blogDao);
    }

    private void pushBlogToRedis(BlogDao blogDao) {
        redisTemplate.opsForList().rightPush(cacheBlogListKey, blogDao);
    }

    private void deleteBlogFromCache(Long blogId) {
        redisTemplate.opsForHash().delete(cacheBlogInfoKey, blogId);
    }
}
