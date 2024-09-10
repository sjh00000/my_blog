package com.blog.sun.mapper;

import com.blog.sun.common.dao.UserDao;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

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
    void registerUser(@Param("userDao")UserDao userDao);

    List<UserDao> checkUserExist(@Param("username")String username, @Param("email")String email);

    boolean changeUserLoginState(@Param("userId")Long userId);

    UserDao getUserByUserName(@Param("username")String userName);

    UserDao getUserByUserId(@Param("userId")Long userId);
}
