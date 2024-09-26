package com.blog.sun.service;

import com.blog.sun.common.dao.UserDao;
import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.sun.common.dto.LoginDto;
import com.blog.sun.common.dto.RegisterDto;
import com.blog.sun.common.vo.UserVo;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author sjh
 * @since 2024-05-16
 */
public interface UserService extends IService<UserDao> {

    //注册新用户
    Integer registerUser(RegisterDto registerDto);

    //登录用户
    UserVo loginByUserName(LoginDto loginDto);

    //退出登录
    void logoutByName();

    UserVo getUserInformation(String username);
}
