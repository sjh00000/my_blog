package com.markerhub.shiro;

import cn.hutool.core.bean.BeanUtil;
import com.markerhub.entity.User;
import com.markerhub.service.UserService;
import com.markerhub.util.JwtUtils;
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
    private  final JwtUtils jwtUtils;
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
        JwtToken jwt = (JwtToken) token;
        String userId = jwtUtils.getClaimByToken((String) jwt.getPrincipal()).getSubject();
        User user = userService.getById(Long.parseLong(userId));
        log.info("user:{}",user);
        if(user == null) {
            throw new UnknownAccountException("账户不存在！");
        }
        if(user.getStatus() == -1) {
            throw new LockedAccountException("账户已被锁定！");
        }
        AccountProfile profile = new AccountProfile();
        BeanUtil.copyProperties(user, profile);

        return new SimpleAuthenticationInfo(profile, jwt.getCredentials(), getName());
    }
}