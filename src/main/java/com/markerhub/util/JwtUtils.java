package com.markerhub.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Date;


/**
 * The type Jwt utils.
 */
@Slf4j
@Data
@Component
@ConfigurationProperties(prefix = "markerhub.jwt")
public class JwtUtils {

    private String secret;
    private long expire;
    private String header;

    /**
     * 生成jwt token
     *
     * @param userId the user id
     * @return the string
     */
    public String generateToken(long userId) {
        Date nowDate = new Date();
        //过期时间
        Date expireDate = new Date(nowDate.getTime() + expire * 1000);

        return Jwts.builder()
                .setHeaderParam("typ", "JWT")           //设置jwt头部参数类型为 JWT
                .setSubject(userId+"")                        //设置主题（subject），这里是用户 ID
                .setIssuedAt(nowDate)                         //设置 JWT 的签发时间
                .setExpiration(expireDate)                    //设置 JWT 的过期时间
                .signWith(SignatureAlgorithm.HS512, secret)   //使用指定的签名算法（HS512）和密钥，对JWT进行签名。
                .compact();                                   //生成并返回紧凑的 JWT 字符串
    }

    /**
     * 获取jwt 声明
     *
     * @param token the token
     * @return claim by token
     */
    public Claims getClaimByToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();
        }catch (Exception e){
            log.debug("解析token失败 ：", e);
            return null;
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