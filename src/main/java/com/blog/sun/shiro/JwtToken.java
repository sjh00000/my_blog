package com.blog.sun.shiro;

import lombok.Getter;
import org.apache.shiro.authc.AuthenticationToken;

public class JwtToken implements AuthenticationToken {
    private final String token;
    @Getter
    private final String flag;
    public JwtToken(String token, String flag) {
        this.token = token;
        this.flag = flag;
    }
    @Override
    public Object getPrincipal() {
        return token;
    }
    @Override
    public Object getCredentials() {
        return token;
    }

}