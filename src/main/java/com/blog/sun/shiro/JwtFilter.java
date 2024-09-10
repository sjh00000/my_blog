package com.blog.sun.shiro;

import cn.hutool.json.JSONUtil;
import com.blog.sun.common.resp.Result;
import com.blog.sun.util.JwtUtils;
import com.blog.sun.util.ShiroUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.ExpiredCredentialsException;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;


/**
 * The type Jwt filter.
 */
@Slf4j
@Component
public class JwtFilter extends AuthenticatingFilter {
    private final JwtUtils jwtUtils;

    @Autowired
    public JwtFilter(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    //从请求头中捕获jwt令牌并创建一个token
    @Override
    protected AuthenticationToken createToken(ServletRequest servletRequest, ServletResponse servletResponse) {
        // 获取 token
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String jwt = request.getHeader("Authorization");
        String flag = request.getHeader("TokenType");
        if (!StringUtils.hasLength(jwt)) {
            return null;
        }
        return new JwtToken(jwt, flag);
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
        if (!StringUtils.hasLength(accessToken)) {
            return true;
        } else {
            // 判断是否存在和已过期
            String flagAccessToken = "1";
            Claims accessClaim = jwtUtils.getClaimByToken(accessToken, flagAccessToken);
            String flagRefreshToken = "2";
            Claims refreshClaim = jwtUtils.getClaimByToken(refreshToken, flagRefreshToken);
            //accessToken已过期
            if (accessClaim == null || jwtUtils.isTokenExpired(accessClaim.getExpiration())) {
                //refreshToken未过期
                if (refreshClaim!=null && !jwtUtils.isTokenExpired(refreshClaim.getExpiration())) {
                    long userId = Long.parseLong(refreshClaim.getSubject());
                    response.setHeader("Authorization", jwtUtils.generateAccessToken(userId));
                }else{
                    throw new ExpiredCredentialsException("token已失效，请重新登录！");
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

    /**
     * 对跨域提供支持
     */
    @Override
    protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest httpServletRequest = WebUtils.toHttp(request);
        HttpServletResponse httpServletResponse = WebUtils.toHttp(response);
        httpServletResponse.setHeader("Access-control-Allow-Origin", httpServletRequest.getHeader("Origin"));
        httpServletResponse.setHeader("Access-Control-Allow-Methods", "GET,POST,OPTIONS,PUT,DELETE");
        httpServletResponse.setHeader("Access-Control-Allow-Headers", httpServletRequest.getHeader("Access-Control-Request-Headers"));
        // 跨域时会首先发送一个OPTIONS请求，这里我们给OPTIONS请求直接返回正常状态
        if (httpServletRequest.getMethod().equals(RequestMethod.OPTIONS.name())) {
            httpServletResponse.setStatus(org.springframework.http.HttpStatus.OK.value());
            return false;
        }
        return super.preHandle(request, response);
    }
}