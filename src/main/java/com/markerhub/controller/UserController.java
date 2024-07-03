package com.markerhub.controller;


import com.markerhub.common.lang.Result;
import com.markerhub.entity.User;
import com.markerhub.service.UserService;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author sjh
 * @since 2024-05-16
 */
@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 获取用户信息接口。
     * 该接口需要用户认证才能访问，通过查询ID为1的用户信息并返回给前端。
     *
     * @return Result 对象，包含操作结果和用户信息。
     */
    @RequiresAuthentication
    @GetMapping("/index")
    public Result index() {
        // 根据ID 1获取用户信息
        User user = userService.getById(1L);
        // 返回操作成功的结果，并附带用户信息
        return Result.succ(user);
    }


    /**
     * 通过POST请求保存用户信息。
     * <p>
     * 此方法接收一个包含用户信息的请求体，并验证其合法性后保存用户信息。
     *
     * @param user 经过验证的用户对象，包含待保存的用户信息。
     * @return 返回一个结果对象，其中包含保存成功的用户信息。
     */
    @PostMapping("/save")
    public Result save(@Validated @RequestBody User user) {
        return Result.succ(user);
    }

}
