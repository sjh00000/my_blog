package com.blog.sun.controller;

import com.blog.sun.common.dto.LoginDto;
import com.blog.sun.common.dto.RegisterDto;
import com.blog.sun.common.resp.Result;
import com.blog.sun.common.vo.UserVo;
import com.blog.sun.service.UserService;
import com.blog.sun.util.JwtUtils;
import com.blog.sun.util.ShiroUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.ExpiredCredentialsException;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Slf4j
@RestController
public class AccountController {

    @Autowired
    private UserService userService;
    @Autowired
    private  JwtUtils jwtUtils;

    /**
     * 处理用户登录请求。
     * 通过验证用户名和密码，如果验证成功，返回包含用户信息和JWT令牌的结果。
     *
     */
    @PostMapping("/account/login")
    public Result accountLogin(@Validated @RequestBody LoginDto loginDto, HttpServletResponse response) {
        log.info("进入controller");
        // 根据用户名查询并验证用户
        UserVo userVo = userService.loginByUserName(loginDto);
        if(userVo==null){
            return Result.fail("登录失败，请检查用户名密码是否正确或已经登录");
        }
        // 生成JWT令牌，用于用户身份验证。
        String accessToken = jwtUtils.generateAccessToken(userVo.getId());
        String refreshToken = jwtUtils.generateRefreshToken(userVo.getId());
        log.info("生成的accessToken为：{}", accessToken);
        log.info("生成的refreshToken为：{}", refreshToken);
        // 设置HTTP响应头，将JWT令牌返回给客户端。
        response.setHeader("Authorization", accessToken);
        response.setHeader("Access-control-Expose-Headers", "Authorization");
        // 创建一个Cookie对象，用于存储刷新令牌
        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
        // 设置Cookie的路径，确保其在整个网站范围内可用
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        response.addCookie(refreshTokenCookie);

        // 返回成功结果，包含用户基本信息。
        return Result.succ(userVo);
    }

    @RequiresAuthentication
    @PostMapping("/account/logout")
    public Result accountLogout() {
        userService.logoutByName();
        return Result.succ(null);
    }

//    @PostMapping("/account/refreshToken")
//    public Result accountRefreshToken(HttpServletResponse response) {
//        log.info("此时登录中的用户信息有{}", ShiroUtil.getProfile());
//        //此时鉴权成功，重新获取新的accessToken
//        Long userId = ShiroUtil.getProfile().getId();
//        String accessToken = jwtUtils.generateAccessToken(userId);
//        response.setHeader("Authorization", accessToken);
//        return Result.succ(null);
//    }

    @PostMapping("/account/register")
    public Result accountRegister(@Valid @RequestBody RegisterDto registerDto) {
        // 调用UserService的注册方法
        int result = userService.registerUser(registerDto);
        if(result == 1){
            return Result.fail("用户或邮箱已存在");

        }else {
            return Result.succ(null);
        }
    }

}