package com.markerhub.controller;

import cn.hutool.core.map.MapUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.markerhub.common.vo.LoginVo;
import com.markerhub.common.lang.Result;
import com.markerhub.entity.User;
import com.markerhub.service.UserService;
import com.markerhub.util.JwtUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@RestController
public class AccountController {
    private final UserService userService;
    private final JwtUtils jwtUtils;
    @Autowired
    public AccountController(UserService userService, JwtUtils jwtUtils) {
        this.userService = userService;
        this.jwtUtils = jwtUtils;
    }

    /**
     * 处理用户登录请求。
     * 通过验证用户名和密码，如果验证成功，返回包含用户信息和JWT令牌的结果。
     *
     * @param loginDto 包含用户名和密码的登录数据传输对象。
     * @param response HTTP响应对象，用于设置JWT令牌。
     * @return 如果登录成功，返回包含用户信息的结果；如果登录失败，返回错误信息。
     */
    @PostMapping("/login")
    public Result login(@Validated @RequestBody LoginVo loginDto, HttpServletResponse response) {
        // 根据用户名查询用户，确保用户存在。
        User user = userService.getOne(new QueryWrapper<User>().eq("username", loginDto.getUsername()));
        Assert.notNull(user, "用户不存在");

        // 验证密码是否正确。如果密码不匹配，返回错误信息。
        if(!user.getPassword().equals(SecureUtil.md5(loginDto.getPassword()))){
            System.out.println(
                    user.getPassword() + ":" + SecureUtil.md5(loginDto.getPassword())
            );
            return Result.fail("密码不正确");
        }

        // 生成JWT令牌，用于用户身份验证。
        String jwt = jwtUtils.generateToken(user.getId());

        // 设置HTTP响应头，将JWT令牌返回给客户端。
        response.setHeader("Authorization", jwt);
        response.setHeader("Access-control-Expose-Headers", "Authorization");

        // 返回成功结果，包含用户基本信息。
        return Result.succ(MapUtil.builder()
                .put("id", user.getId())
                .put("username", user.getUsername())
                .put("avatar", user.getAvatar())
                .put("email", user.getEmail())
                .map()
        );
    }

    /**
     * 用户退出登录接口。
     * <p>
     * 通过调用SecurityUtils.getSubject().logout()方法，实现用户退出登录的功能。
     * 主要清除用户的认证信息，使得用户需要重新登录才能访问需要认证的资源。
     *
     * @return 返回一个表示操作成功的Result对象，其中data字段为null。
     */
    @RequiresAuthentication
    @GetMapping("/logout")
    public Result logout() {
        // 清除认证信息以及缓存
        SecurityUtils.getSubject().logout();
        return Result.succ(null);
    }


}