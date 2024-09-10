package com.blog.sun.service.impl;

import cn.hutool.crypto.SecureUtil;
import com.blog.sun.common.dto.LoginDto;
import com.blog.sun.common.dao.UserDao;
import com.blog.sun.common.dto.RegisterDto;
import com.blog.sun.common.vo.UserVo;
import com.blog.sun.mapper.UserMapper;
import com.blog.sun.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.sun.util.JwtUtils;
import com.hy.corecode.idgen.WFGIdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author sjh
 * @since 2024-05-16
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDao> implements UserService {

    @Autowired
    UserMapper userMapper;
    @Autowired
    JwtUtils jwtUtils;
    @Autowired
    private WFGIdGenerator wFGIdGenerator;

    @Override
    public Integer registerUser(RegisterDto registerDto) {
        UserDao userDao = new UserDao();
        userDao.setUsername(registerDto.getUsername());
        userDao.setStatus(0);
        userDao.setPassword(SecureUtil.md5(registerDto.getPassword()));
        userDao.setEmail(registerDto.getEmail());
        userDao.setCreated(LocalDateTime.now());
        //检查用户名，邮箱是否注册
        List<UserDao> userDaoList =userMapper.checkUserExist(userDao.getUsername(), userDao.getEmail());
        if (!userDaoList.isEmpty()) {
            log.info("注册失败-用户名或邮箱已存在");
            return 1;
        }
        //雪花算法生成唯一id
        userDao.setId(wFGIdGenerator.next());
        //注册用户
        userMapper.registerUser(userDao);
        log.info("注册成功{}",userDao);
        return 0;

    }

    @Override
    public UserVo loginByUserName(LoginDto loginDto) {
        UserDao userDao = userMapper.getUserByUserName(loginDto.getUsername());
        if (userDao == null) {
            log.info("登录失败-未找到该用户");
            return null;
        } else {
            log.info("找到登录用户{}", userDao);
        }
        // 验证密码是否正确。如果密码不匹配，返回错误信息。
        if(!userDao.getPassword().equals(SecureUtil.md5(loginDto.getPassword()))){
            log.info("登录失败-用户密码错误");
            return null;
        }
        //检验登录状态
        if(userDao.getStatus()==1){
            log.info("登录失败-用户已经登录");
            return null;
        }else{
            userMapper.changeUserLoginState(userDao.getId());
        }
        UserVo userVo= new UserVo();
        BeanUtils.copyProperties(userDao,userVo);
        return userVo;
    }

    @Override
    public void logoutById(Long userId) {
        // 清除认证信息以及缓存
        SecurityUtils.getSubject().logout();
        UserDao userDao =  userMapper.getUserByUserId(userId);
        if(userDao.getStatus()==1){
            userMapper.changeUserLoginState(userId);
        }else{
            log.info("用户未登录");
        }
    }

    @Override
    public UserVo getUserInformation(Long userId) {
        UserDao userDao =  userMapper.getUserByUserId(userId);
        UserVo userVo = new UserVo();
        BeanUtils.copyProperties(userDao,userVo);
        return  userVo;
    }
}
