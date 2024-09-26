package com.blog.sun.shiro;

import cn.hutool.json.JSONUtil;
import com.blog.sun.common.resp.Result;
import com.blog.sun.test.ErrorBean;
import com.blog.sun.util.JwtUtils;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * The type Jwt filter.
 */
@Slf4j
@Component
public class JwtFilter extends AuthenticatingFilter {
    private final JwtUtils jwtUtils;
    private final ErrorBean errorBean;

    @Autowired
    public JwtFilter(JwtUtils jwtUtils, ErrorBean errorBean) {
        this.jwtUtils = jwtUtils;
        this.errorBean = errorBean;
    }

    //从请求头中捕获jwt令牌并创建一个token
    @Override
    protected AuthenticationToken createToken(ServletRequest servletRequest, ServletResponse servletResponse) {
        // 获取 token
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String jwt = "";
        if (response.getHeader("Authorization") != null && !response.getHeader("Authorization").isEmpty()) {
            jwt = response.getHeader("Authorization");
            return new JwtToken(jwt, "1");
        }else{
            jwt = request.getHeader("Authorization");
            if (!StringUtils.hasLength(jwt)) {
                return null;
            }else{
                return new JwtToken(jwt, "1");
            }
        }
    }

    //进行拦截校验
    @Override
    protected boolean onAccessDenied(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String accessToken = request.getHeader("Authorization");
        Cookie[] cookies = request.getCookies();
        String refreshToken = "";
        //找到refreshToken
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    // 获取 refreshToken 的值
                    refreshToken = cookie.getValue();
                    log.info("refreshToken为: " + refreshToken);
                    // 你可以在这里处理 refreshToken，例如验证或更新
                    break; // 找到 refreshToken 后退出循环
                }
            }
        }
        log.info("JwtFilter accessToken:{}", accessToken);
        log.info("JwtFilter refreshToken:{}", refreshToken);
        if (!StringUtils.hasLength(accessToken)) {
            return true;
        } else {
            // 判断是否存在和已过期
            String flagAccessToken = "1";
            Claims accessClaim = jwtUtils.getClaimByToken(accessToken, flagAccessToken);
            String flagRefreshToken = "2";
            Claims refreshClaim=null;
            //refreshToken还存在cookie中
            if(!refreshToken.isEmpty()){
               refreshClaim = jwtUtils.getClaimByToken(refreshToken, flagRefreshToken);
            }
            //accessToken已过期
            if (accessClaim == null || jwtUtils.isTokenExpired(accessClaim.getExpiration())) {
                //refreshToken未过期
                if (refreshClaim != null && !jwtUtils.isTokenExpired(refreshClaim.getExpiration())) {
                    long userId = Long.parseLong(refreshClaim.getSubject());
                    log.info("目前的accessToken已过期，但是refreshToken没有过期");
                    response.setHeader("Authorization", jwtUtils.generateAccessToken(userId));
                } else {
                    log.info("都过期了");
                    //1--accessToken过期的情况下，refreshToken也过期了 2--refreshToken在cookie中找不到
                    errorBean.throwException();
                    return true;
                }

            }
        }
        // 执行身份验证
       return executeLogin(servletRequest, servletResponse);
    }

    //处理身份验证异常
    @Override
    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e, ServletRequest request, ServletResponse response) {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        try {
            //处理登录失败的异常
            Throwable throwable = e.getCause() == null ? e : e.getCause();
            Result r = Result.fail(throwable.getMessage());
            String json = JSONUtil.toJsonStr(r);
            httpResponse.getWriter().print(json);
        } catch (IOException e1) {
            log.info("登录失败：{}", e1.getMessage());
        }
        return false;
    }

}