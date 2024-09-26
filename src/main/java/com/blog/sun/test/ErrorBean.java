package com.blog.sun.test;

import com.blog.sun.annotation.HandleFrontMvcException;
import org.apache.shiro.authc.ExpiredCredentialsException;
import org.springframework.stereotype.Component;

@Component
public class ErrorBean {
    @HandleFrontMvcException
    public void throwException() {
        throw new ExpiredCredentialsException("token已失效，请重新登录！");
    }

}
