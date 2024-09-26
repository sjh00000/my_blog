package com.blog.sun.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.sun.common.dto.BlogDto;
import com.blog.sun.common.dto.CommentDto;
import com.blog.sun.common.resp.Result;
import com.blog.sun.common.vo.BlogVo;
import com.blog.sun.common.vo.CommentVo;
import com.blog.sun.service.BlogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * @author sjh
 * @since 2024-05-16
 */
@Slf4j
@RestController
public class BlogController {
    private final BlogService blogService;
    @Autowired
    public BlogController(BlogService blogService) {
        this.blogService = blogService;
    }

    /**
     * 获取博客列表接口。
     * 该接口用于分页查询博客列表，默认返回第一页数据，每页显示10条博客记录。
     */
    @GetMapping("/blog/list")
    public Result getBolgList(@RequestParam(defaultValue = "1") Integer currentPage) {
        // 初始化分页对象，指定当前页码和每页显示的记录数
        Page<BlogVo> page = new Page<>(currentPage, 10);
        // 调用blogService的page方法进行分页查询，排序方式为按创建时间降序
        IPage<BlogVo> pageData = blogService.getBlogList(page);
        log.info("pageData:{}",pageData.getRecords());
        return Result.succ(pageData);
    }

    /**
     * 先从服务层根据ID查询博客，然后校验博客是否存在，如果不存在则抛出异常，
     * 表示该博客已被删除。如果博客存在，则将博客封装在Result对象中返回。
     */
    @GetMapping("/blog/detail")
    public Result getBlogDetail(@RequestParam Long blogId) {
        // 从服务层根据ID获取博客
        BlogVo blog = blogService.getBlogById(blogId);
        // 返回成功的Result对象，包含博客详情
        return Result.succ(blog);
    }

    /**
     * 编辑博客文章。
     * 如果提供的博客ID不为空，则检查当前用户是否有权限编辑该博客；
     * 如果博客ID为空，则创建一个新的博客并设置默认属性。
     */
    @RequiresAuthentication
    @PostMapping("/blog/edit")
    public Result editBlogDetail(@Validated @RequestBody BlogDto blogDto) {
        Boolean result = blogService.editBlogDetail(blogDto);
        if (result){
            return Result.succ("编辑成功");
        }else {
            return Result.fail("编辑失败");
        }
    }

    /**
     * 搜索博客接口。
     * 根据提供的关键词进行模糊搜索，支持分页查询。
     */
    @PostMapping("/blog/search")
    public Result searchBlogs(@RequestParam String keyword,
                             @RequestParam(defaultValue = "1") Integer currentPage) {
        // 初始化分页对象，指定当前页码和每页显示的记录数
        Page<BlogVo> page = new Page<>(currentPage, 10);
        // 执行分页模糊搜索
        IPage<BlogVo> pageData = blogService.searchBlogsLikeTitleOrContent(page,keyword);
        // 返回查询结果
        return Result.succ(pageData);
    }

    /**
     * 根据标签获取博客列表接口。
     */
    @GetMapping("/blog/label")
    public Result queryByLabel(@RequestParam String label,
                               @RequestParam(defaultValue = "1") Integer currentPage) {
        // 初始化分页对象，指定当前页码和每页显示的记录数
        Page<BlogVo> page = new Page<>(currentPage, 10);
        IPage<BlogVo> pageData = blogService.queryByLabel(page, label);
        // 返回查询结果
        log.info("pageData:{}",pageData.getRecords());
        return Result.succ(pageData);
    }

    /**
     * 查询所有不重复的博客标签接口。
     */
    @GetMapping("/blog/tags")
    public Result queryAllTags() {
        // 执行查询，由于只关心标签，不需要分页
        List<String> tags = blogService.queryAllTags();
        // 返回所有不重复的标签列表
        return Result.succ(tags);
    }

    /**
     * 查询评论列表
     */
    @GetMapping("blog/comment")
    public Result getCommentList(@RequestParam Long blogId,@RequestParam Long parentId) {
        List<CommentVo> commentList = blogService.getCommentList(blogId, parentId);
        return Result.succ(commentList);
    }

    /**
     * 添加评论
     */
    @RequiresAuthentication
    @GetMapping("blog/comment/edit")
    public Result editComment(@RequestBody CommentDto commentDto) {
        // 执行分页查询，并返回查询结果
        blogService.editComment(commentDto);
        return Result.succ(null);
    }

}
