package com.blog.sun.controller;


import com.blog.sun.common.dao.UserDao;
import com.blog.sun.common.vo.UserVo;
import com.blog.sun.service.UserService;
import com.blog.sun.common.resp.Result;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author sjh
 * @since 2024-05-16
 */
@RestController
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 获取用户信息接口。
     * 该接口需要用户认证才能访问，通过查询ID为1的用户信息并返回给前端。
     */
    @RequiresAuthentication
    @GetMapping("/user/information")
    public Result getUserInformation(@RequestParam Long userId) {
        UserVo userVo = userService.getUserInformation(userId);
        // 返回操作成功的结果，并附带用户信息
        return Result.succ(userVo);
    }


}
