package com.blog.sun.util;

import com.blog.sun.shiro.AccountProfile;
import org.apache.shiro.SecurityUtils;

public class ShiroUtil {

    //获取登陆验证成功后保存的用户信息
    public static AccountProfile getProfile() {
        return (AccountProfile) SecurityUtils.getSubject().getPrincipal();
    }

}