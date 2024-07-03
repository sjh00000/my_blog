package com.markerhub.test;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class ConfigValue {
    @Value("${shiro-redis.redis-manager.host}")
    private String port;
}