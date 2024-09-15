package com.blog.sun.service.impl;

import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.sun.common.dao.UserDao;
import com.blog.sun.common.dto.LoginDto;
import com.blog.sun.common.dto.RegisterDto;
import com.blog.sun.common.vo.UserVo;
import com.blog.sun.mapper.UserMapper;
import com.blog.sun.service.UserService;
import com.blog.sun.util.JwtUtils;
import com.hy.corecode.idgen.WFGIdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final String cacheUserInfoKey = "user:info";

    @Transactional
    @Override
    public Integer registerUser(RegisterDto registerDto) {
        UserDao userDao = new UserDao();
        userDao.setUsername(registerDto.getUsername());
        userDao.setStatus(0);
        userDao.setPassword(SecureUtil.md5(registerDto.getPassword()));
        userDao.setEmail(registerDto.getEmail());
        userDao.setCreated(Date.from(Instant.now()));
        //检查用户名，邮箱是否注册
        if (redisTemplate.opsForHash().hasKey(cacheUserInfoKey, userDao.getUsername())) {
            log.info("注册失败-用户名或邮箱已存在");
            return 1;
        }
        List<UserDao> userDaoList = userMapper.checkUserExist(userDao.getUsername(), userDao.getEmail());
        if (!userDaoList.isEmpty()) {
            log.info("注册失败-用户名或邮箱已存在");
            return 1;
        }
        //雪花算法生成唯一id
        userDao.setId(wFGIdGenerator.next());
        //注册用户
        userMapper.registerUser(userDao);
        // 存储到Redis中过期时间1天
        saveUserInfoToRedis(userDao);
        redisTemplate.expire(cacheUserInfoKey, 1, TimeUnit.HOURS);
        log.info("注册成功{}", userDao);
        return 0;

    }

    @Override
    public UserVo loginByUserName(LoginDto loginDto) {
        String username = loginDto.getUsername();
        UserDao userDao = getUserFromRedis(username);

        if (userDao == null) {
            log.info("Redis中未找到目标登录用户");
            //在数据库中查找
            userDao = userMapper.getUserByUserName(username);
            if (userDao == null) {
                log.info("登录失败-未找到目标登录用户");
                return null;
            } else {
                log.info("数据库找到目标登录用户 {}", userDao);
            }
        } else {
            log.info("Redis中找到目标登录用户{}", userDao);
        }

        // 验证密码是否正确。如果密码不匹配，返回错误信息。
        if (!userDao.getPassword().equals(SecureUtil.md5(loginDto.getPassword()))) {
            log.info("登录失败-用户密码错误");
            return null;
        }

        // 检验登录状态
        if (userDao.getStatus() == 1) {
            log.info("登录失败-用户已经登录");
            return null;
        } else {
            //先删除缓存再更新数据库，防止缓存和数据库不一致
            deleteBlogFromCache(username);
            userMapper.changeUserLoginState(userDao.getId());
            userDao.setStatus(1);
        }

        UserVo userVo = new UserVo();
        BeanUtils.copyProperties(userDao, userVo);
        return userVo;
    }

    @Override
    public void logoutByName(String username) {
        // 清除认证信息以及缓存
        SecurityUtils.getSubject().logout();
        UserDao userDao = getUserFromRedis(username);
        if (userDao == null) {
            log.info("Redis中未找到目标用户");
            userDao = userMapper.getUserByUserName(username);
        } else {
            log.info("Redis中找到目标用户{}", userDao);
        }
        if (userDao != null && userDao.getStatus() == 1) {
            userMapper.changeUserLoginState(userDao.getId());
            userDao.setStatus(0);
            deleteBlogFromCache(username);
            redisTemplate.expire(cacheUserInfoKey, 2, TimeUnit.DAYS);
        } else {
            log.info("用户未登录");
        }

    }

    @Override
    public UserVo getUserInformation(String username) {
        UserDao userDao = getUserFromRedis(username);
        if (userDao == null) {
            log.info("Redis中未找到目标用户");
            userDao = userMapper.getUserByUserName(username);
            saveUserInfoToRedis(userDao);
            redisTemplate.expire(cacheUserInfoKey, 2, TimeUnit.DAYS);
        } else {
            log.info("Redis中找到目标用户{}", userDao);
        }
        UserVo userVo = new UserVo();
        BeanUtils.copyProperties(userDao, userVo);
        return userVo;
    }

    private UserDao getUserFromRedis(String username) {
        return (UserDao) redisTemplate.opsForHash().get(cacheUserInfoKey, username);
    }

    private void saveUserInfoToRedis(UserDao userDao) {
        redisTemplate.opsForHash().put(cacheUserInfoKey, userDao.getUsername(), userDao);
    }

    private void deleteBlogFromCache(String username) {
        redisTemplate.opsForHash().delete(cacheUserInfoKey, username);
    }
}
