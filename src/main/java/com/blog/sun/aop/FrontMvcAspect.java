package com.blog.sun.aop;

import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.ExpiredCredentialsException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletResponse;

@Slf4j
@Aspect
@Component
public class FrontMvcAspect {

    @Around("@annotation(com.blog.sun.annotation.HandleFrontMvcException)")
    public Object handleJwtException(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("进入切面方法");
        try {
            return joinPoint.proceed(); // 正常执行被代理的方法
        } catch (ExpiredCredentialsException ex) {
            log.info("捕获到 ExpiredCredentialsException: {}", ex.getMessage());

            // 获取 HttpServletResponse
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (requestAttributes != null) {
                HttpServletResponse response = requestAttributes.getResponse();
                if (response != null) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                }
            }
            return null; // 返回自定义响应
        }
    }

}
