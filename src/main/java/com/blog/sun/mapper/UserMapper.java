package com.blog.sun.mapper;

import com.blog.sun.common.dao.UserDao;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author sjh
 * @since 2024-05-16
 */
@Mapper
public interface UserMapper extends BaseMapper<UserDao> {
    void registerUser(UserDao userDao);

    UserDao checkUserExist(String username, String email);

    boolean changeUserLoginState(Long userId);

    UserDao getUserByUserName(String userName);

    UserDao getUserByUserId(Long userId);
}
