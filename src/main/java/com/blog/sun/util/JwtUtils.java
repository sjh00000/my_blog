package com.blog.sun.util;
import com.blog.sun.annotation.HandleFrontMvcException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.ExpiredCredentialsException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Objects;


/**
 * The type Jwt utils.
 */
@Slf4j
@Data
@Component
@ConfigurationProperties(prefix = "sun-blog.jwt")
public class JwtUtils {
    @Value("access-expire")
    private String accessSecret;
    @Value("refresh-expire")
    private String refreshSecret;
    @Value("access-expire")
    private String accessExpire;
    @Value("refresh-expire")
    private String refreshExpire;
    private String header;

    /**
     * 生成accessToken
     *
     */
    public String generateAccessToken(long userId) {
        Date nowDate = new Date();
        //过期时间accessExpire* 1000
        Date expireDate = new Date(nowDate.getTime() + Long.parseLong(accessExpire)* 1000);

        return Jwts.builder()
                .setHeaderParam("typ", "JWT")           //设置jwt头部参数类型为 JWT
                .setSubject(userId+"")                        //设置主题（subject），这里是用户 ID
                .setIssuedAt(nowDate)                         //设置 JWT 的签发时间
                .setExpiration(expireDate)                    //设置 JWT 的过期时间
                .signWith(SignatureAlgorithm.HS512, accessSecret)   //使用指定的签名算法（HS512）和密钥，对JWT进行签名。
                .compact();                                   //生成并返回紧凑的 JWT 字符串
    }

    /**
     * 生成refreshToken
     *
     */
    public String generateRefreshToken(long userId) {
        Date nowDate = new Date();
        //过期时间refreshExpire* 1000
        Date expireDate = new Date(nowDate.getTime() + Long.parseLong(refreshExpire) * 1000);

        return Jwts.builder()
                .setHeaderParam("typ", "JWT")           //设置jwt头部参数类型为 JWT
                .setSubject(userId+"")                        //设置主题（subject），这里是用户 ID
                .setIssuedAt(nowDate)                         //设置 JWT 的签发时间
                .setExpiration(expireDate)                    //设置 JWT 的过期时间
                .signWith(SignatureAlgorithm.HS512, refreshSecret)   //使用指定的签名算法（HS512）和密钥，对JWT进行签名。
                .compact();                                   //生成并返回紧凑的 JWT 字符串
    }

    /**
     * 获取jwt 声明
     *
     * @param token the token
     * @return claim by token
     */
    @HandleFrontMvcException
    public Claims getClaimByToken(String token,String flag) {
        log.info("token:"+token+" "+"flag:"+flag);
        try {
            return Jwts.parser()
                    //flag--1 accessSecret  flag--2 refreshSecret
                    .setSigningKey(Objects.equals(flag, "1") ?accessSecret:refreshSecret)
                    .parseClaimsJws(token)
                    .getBody();
        }catch (Exception e){
            log.info("解析token失败 ：");
            throw new ExpiredCredentialsException("token已失效，请重新登录！");
        }
    }

    /**
     * token是否过期
     *
     * @param expiration the expiration
     * @return true ：过期
     */
    public boolean isTokenExpired(Date expiration) {
        return expiration.before(new Date());
    }
}