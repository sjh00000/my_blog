package com.markerhub.controller;


import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.markerhub.common.lang.Result;
import com.markerhub.entity.Blog;
import com.markerhub.service.BlogService;
import com.markerhub.util.ShiroUtil;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author sjh
 * @since 2024-05-16
 */
@RestController
public class BlogController {
    private final BlogService blogService;
    @Autowired
    public BlogController(BlogService blogService) {
        this.blogService = blogService;
    }

    /**
     * 获取博客列表接口。
     * 该接口用于分页查询博客列表，默认返回第一页数据，每页显示5条博客记录。
     *
     * @param currentPage 当前页码，使用@RequestParam注解指定默认值为1，表示默认返回第一页数据。
     * @return 返回Result对象，其中包含分页查询结果。
     *         Result对象的succ方法用于包装IPage<Blog>对象并返回成功响应。
     */
    @GetMapping("/blogs")
    public Result list(@RequestParam(defaultValue = "1") Integer currentPage) {
        // 初始化分页对象，指定当前页码和每页显示的记录数
        Page<Blog> page = new Page<>(currentPage, 5);

        // 调用blogService的page方法进行分页查询，排序方式为按创建时间降序
        IPage<Blog> pageData = blogService.page(page, new QueryWrapper<Blog>().orderByDesc("created"));

        // 返回查询结果
        return Result.succ(pageData);
    }


    /**
     * 获取博客详情
     *
     * @param id 博客的唯一标识符
     * @return 包含博客详情的Result对象
     *
     * 该方法通过GET请求访问，用于获取指定ID的博客详细信息。
     * 先从服务层根据ID查询博客，然后校验博客是否存在，如果不存在则抛出异常，
     * 表示该博客已被删除。如果博客存在，则将博客封装在Result对象中返回。
     */
    @GetMapping("/blog/{id}")
    public Result detail(@PathVariable(name = "id") Long id) {
        // 从服务层根据ID获取博客
        Blog blog = blogService.getById(id);
        // 校验博客是否存在，不存在则抛异常
        Assert.notNull(blog, "该博客已被删除");

        // 返回成功的Result对象，包含博客详情
        return Result.succ(blog);
    }


    /**
     * 编辑博客文章。
     * 通过POST请求对已存在的博客进行更新，或创建新的博客。
     * 如果提供的博客ID不为空，则检查当前用户是否有权限编辑该博客；
     * 如果博客ID为空，则创建一个新的博客并设置默认属性。
     *
     * @param blog 包含博客详细信息的实体类，可能包含ID用于更新现有博客。
     * @return 返回一个表示操作结果的对象，成功时不含具体数据。
     */
    @RequiresAuthentication
    @PostMapping("/blog/edit")
    public Result edit(@Validated @RequestBody Blog blog) {
        Blog temp;
        // 根据博客ID判断是更新现有博客还是创建新博客
        if(blog.getId() != null) {
            temp = blogService.getById(blog.getId());
            // 校验发送的博客是不是自己写的
            System.out.println("是"+ShiroUtil.getProfile().getId()+"写的");
            System.out.println(blog.getLabel());
            Assert.isTrue(temp.getUserId().longValue() == ShiroUtil.getProfile().getId().longValue(), "没有权限编辑");
        } else {
            temp = new Blog();
            temp.setUserId(ShiroUtil.getProfile().getId());
            temp.setCreated(LocalDateTime.now());
//            temp.setStatus(0);
        }

        // 复制博客属性到临时对象，忽略特定属性以防止覆盖
        BeanUtil.copyProperties(blog, temp, "id", "userId", "created", "status");
        blogService.saveOrUpdate(temp);

        return Result.succ(null);
    }

    /**
     * 搜索博客接口。
     * 根据提供的关键词进行模糊搜索，支持分页查询。
     *
     * @param keyword 搜索关键词
     * @param currentPage 当前页码，默认为1
     * @return 分页的搜索结果
     */
    @GetMapping("/blogs/search")
    public Result searchBlogs(@RequestParam String keyword,
                             @RequestParam(defaultValue = "1") Integer currentPage) {
        // 初始化分页对象，指定当前页码和每页显示的记录数
        Page<Blog> page = new Page<>(currentPage, 5);

        // 构建查询条件，使用like关键字进行模糊匹配标题和内容
        QueryWrapper<Blog> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("title", keyword).or().like("content", keyword);
        queryWrapper.orderByDesc("created"); // 按创建时间降序排列

        // 执行分页模糊搜索
        IPage<Blog> pageData = blogService.page(page, queryWrapper);

        // 返回查询结果
        return Result.succ(pageData);
    }

    /**
     * 根据标签获取博客列表接口。
     * 支持分页查询，每页默认显示5条记录。
     *
     * @param label 博客的标签
     * @param currentPage 当前页码，默认为1
     * @return 分页的博客列表结果
     */
    @GetMapping("/blogs/label/{label}")
    public Result listByLabel(@PathVariable String label,
                             @RequestParam(defaultValue = "1") Integer currentPage) {
        // 初始化分页对象，指定当前页码和每页显示的记录数
        Page<Blog> page = new Page<>(currentPage, 5);

        // 构建查询条件，根据标签筛选
        QueryWrapper<Blog> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("label", label); // 精确匹配标签
        queryWrapper.orderByDesc("created"); // 按创建时间降序排列

        // 执行分页查询
        IPage<Blog> pageData = blogService.page(page, queryWrapper);

        // 返回查询结果
        return Result.succ(pageData);
    }

    /**
     * 查询所有不重复的博客标签接口。
     *
     * @return 包含所有不重复标签的Result对象
     */
    @GetMapping("/tags")
    public Result listAllTags() {
        // 使用QueryWrapper来查询并去重标签
        QueryWrapper<Blog> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("distinct label"); // 仅选择不重复的标签字段

        // 执行查询，由于只关心标签，不需要分页
        List<Object> tags = blogService.listObjs(queryWrapper);

        // 返回所有不重复的标签列表
        return Result.succ(tags);
    }




}
