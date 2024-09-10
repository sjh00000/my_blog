package com.blog.sun.shiro;

import cn.hutool.core.bean.BeanUtil;
import com.blog.sun.common.dao.UserDao;
import com.blog.sun.service.UserService;
import com.blog.sun.util.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AccountRealm extends AuthorizingRealm {
    private final JwtUtils jwtUtils;
    private final UserService userService;

    @Autowired
    public AccountRealm(JwtUtils jwtUtils, UserService userService) {
        this.jwtUtils = jwtUtils;
        this.userService = userService;
    }

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof JwtToken;
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        return null;
    }

    //登录认证校验
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        JwtToken jwtToken = (JwtToken) token;
        String userId = jwtUtils.getClaimByToken((String) jwtToken.getPrincipal(), jwtToken.getFlag()).getSubject();
        UserDao userDao = userService.getById(Long.parseLong(userId));
        log.info("user:{}", userDao);
        if (userDao == null) {
            log.info("账户不存在");
            throw new UnknownAccountException("账户不存在！");
        }
        // 创建一个空的账户配置文件，用于存储用户账户的相关信息。
        AccountProfile profile = new AccountProfile();
        BeanUtil.copyProperties(userDao, profile);
        // 返回一个SimpleAuthenticationInfo对象，包含账户配置文件、JWT凭证和命名空间信息。
        // 这是Shiro框架用于进行身份验证的核心对象，它封装了用户的身份验证信息。
        return new SimpleAuthenticationInfo(profile, jwtToken.getCredentials(), getName());

    }
}