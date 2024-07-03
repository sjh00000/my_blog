package com.markerhub.config;

import com.markerhub.shiro.AccountRealm;
import com.markerhub.shiro.JwtFilter;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.spring.web.config.DefaultShiroFilterChainDefinition;
import org.apache.shiro.spring.web.config.ShiroFilterChainDefinition;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.crazycake.shiro.RedisCacheManager;
import org.crazycake.shiro.RedisSessionDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class ShiroConfig {
    private final JwtFilter jwtFilter;
    @Autowired
    public ShiroConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }


     /**
     * 配置并返回一个 SessionManager 实例。
     * 此方法使用 Spring 的 @Bean 注解在应用上下文中定义一个 Bean。
     *
     * @param redisSessionDAO RedisSessionDAO 实例，用于会话数据的存储与检索。
     * @return 返回一个配置好的 DefaultWebSessionManager 实例，用于会话管理。
     *         通过将 session DAO 设置为 RedisSessionDAO，可以实现在 Redis 中存储会话数据。
     */
    @Bean
    public SessionManager sessionManager(RedisSessionDAO redisSessionDAO) {
        DefaultWebSessionManager sessionManager = new DefaultWebSessionManager();
        sessionManager.setSessionDAO(redisSessionDAO);
        return sessionManager;
    }


     /**
     * 配置并初始化安全管理器。
     * 安全管理器是Shiro安全框架的核心组件，负责处理认证、授权、会话管理和缓存等功能。
     *
     * @param accountRealm 负责认证与授权的领域对象。封装了认证授权逻辑及数据源。
     * @param sessionManager 配置用于会话管理的会话管理器，包括会话的创建、过期与存储等。
     * @param redisCacheManager 使用Redis作为缓存管理器，支持对认证、授权等信息的缓存。
     * @return 返回已初始化的安全管理器实例。
     */
    @Bean
    public DefaultWebSecurityManager securityManager(AccountRealm accountRealm,
                                                     SessionManager sessionManager,
                                                     RedisCacheManager redisCacheManager) {
        // 使用账户领域初始化安全管理器
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager(accountRealm);

        // 配置会话管理器以管理会话
        securityManager.setSessionManager(sessionManager);

        // 配置缓存管理器以缓存认证与授权信息
        securityManager.setCacheManager(redisCacheManager);
        return securityManager;
    }


        /**
     * 创建Shiro过滤器链定义。
     * 该方法的目的是配置Shiro过滤器链，以指定哪些URL路径应该受到特定过滤器的处理。
     * 在这个例子中，我们配置了所有路径（"/**"）都应该通过"jwt"过滤器进行处理。
     *
     * @return ShiroFilterChainDefinition 对象，它包含了URL和相应过滤器的映射关系。
     */
    @Bean
    public ShiroFilterChainDefinition shiroFilterChainDefinition() {
        // 定义过滤器链
        DefaultShiroFilterChainDefinition chainDefinition = new DefaultShiroFilterChainDefinition();

        Map<String, String> filterMap = new LinkedHashMap<>();
        filterMap.put("/**", "jwt");

        // 将映射关系添加到链定义中
        chainDefinition.addPathDefinitions(filterMap);
        return chainDefinition;
    }


    /**
     * 创建ShiroFilterFactoryBean实例，用于配置Shiro的过滤器。
     *
     * @param securityManager Shiro的securityManager，用于管理安全相关的操作。
     * @param shiroFilterChainDefinition 过滤器链定义，用于定义哪些URL应该被哪些过滤器处理。
     * @return ShiroFilterFactoryBean实例，配置好过滤器和过滤器链定义。
     * <p>
     * 该方法通过设置securityManager和自定义的过滤器（如JWT过滤器），以及过滤器链定义，
     * 来定制Shiro的过滤器工厂。这样配置的过滤器工厂将在Spring应用程序启动时被使用，
     * 用于处理应用程序的HTTP请求。
     */
    @Bean("shiroFilterFactoryBean")
    public ShiroFilterFactoryBean shiroFilterFactoryBean(SecurityManager securityManager,
                                                         ShiroFilterChainDefinition shiroFilterChainDefinition) {
        ShiroFilterFactoryBean shiroFilter = new ShiroFilterFactoryBean();
        shiroFilter.setSecurityManager(securityManager);

        // 自定义过滤器映射，将"jwt"映射到JWTFilter实例。
        Map<String, Filter> filters = new HashMap<>();
        filters.put("jwt", jwtFilter);
        shiroFilter.setFilters(filters);

        // 设置过滤器链定义，定义哪些URL应该通过哪些过滤器进行处理。
        Map<String, String> filterMap = shiroFilterChainDefinition.getFilterChainMap();
        shiroFilter.setFilterChainDefinitionMap(filterMap);
        return shiroFilter;
    }


}