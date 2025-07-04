package org.example.config;

import org.example.filter.JwtRequestFilter;
import org.example.service.CustomOAuth2UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 配置类。
 * <p>
 * 负责整个应用的安全认证和授权设置，包括密码加密、认证管理器配置和 HTTP 请求的安全规则。
 * 扩展支持 OAuth2 (Google) 登录。
 * </p>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    /**
     * 注入自定义的 JWT 请求过滤器。
     * 该过滤器用于在每个请求到达受保护的端点之前，验证 JWT Token。
     */
    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    /**
     * 注入自定义的 OAuth2 用户服务，用于处理从 Google 获取到的用户信息。
     */
    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    /**
     * 注入自定义的 OAuth2 登录成功处理器，用于在 OAuth2 登录成功后生成 JWT。
     */
    @Autowired
    private OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    public SecurityConfig() {
        logger.info("SecurityConfig 构造函数被调用。");
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        logger.info("AuthenticationManager Bean 被创建。");
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * 配置安全过滤链 (SecurityFilterChain)。
     * <p>
     * 这是定义所有 HTTP 请求安全策略的核心位置。它定义了哪些请求需要认证，哪些可以匿名访问，以及会话管理策略。
     * </p>
     *
     * @param http HttpSecurity 对象，用于构建安全策略。
     * @return 返回配置好的 {@link SecurityFilterChain} 实例。
     * @throws Exception 如果配置出错。
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        logger.info("securityFilterChain Bean 被创建。");
        if (customOAuth2UserService != null) {
            logger.info("customOAuth2UserService 已成功注入，哈希码: {}", customOAuth2UserService.hashCode());
        } else {
            logger.error("customOAuth2UserService 未能成功注入！");
        }
        if (oAuth2LoginSuccessHandler != null) {
            logger.info("oAuth2LoginSuccessHandler 已成功注入，哈希码: {}", oAuth2LoginSuccessHandler.hashCode());
        } else {
            logger.error("oAuth2LoginSuccessHandler 未能成功注入！");
        }
        http
                // 禁用 CSRF (跨站请求伪造) 保护，因为我们使用 JWT，是无状态的，所以不需要 CSRF 保护。
                .csrf(csrf -> csrf.disable())
                // 配置请求授权规则
                .authorizeHttpRequests(authorize -> authorize
                        // 对以下路径的请求允许匿名访问 (无需认证)
                        // 主要包括用户认证(注册/登录)接口、OAuth2 登录相关路径和 API 文档(Swagger)相关路径。
                        .requestMatchers("/api/auth/**", "/oauth2/**", "/login/oauth2/code/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // 除了上面明确放行的路径，所有其他请求都必须经过认证。
                        .anyRequest().authenticated()
                )
                // 配置 OAuth2 登录
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService) // 使用自定义的 OAuth2 用户服务
                        )
                        .successHandler(oAuth2LoginSuccessHandler) // 使用自定义的登录成功处理器
                )
                // 配置会话管理策略
                .sessionManagement(session -> session
                        // 设置会话创建策略为 STATELESS (无状态)。
                        // 这意味着服务器不会创建或使用 HttpSession，每次请求都依赖于 JWT 进行认证。
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        if (customOAuth2UserService == null) {
            logger.error("CustomOAuth2UserService 未能成功注入到 SecurityConfig 中！");
        }
        if (oAuth2LoginSuccessHandler == null) {
            logger.error("OAuth2LoginSuccessHandler 未能成功注入到 SecurityConfig 中！");
        }

        // 将我们自定义的 JWT 过滤器添加到 Spring Security 的过滤器链中。
        // 它会在标准的用户名密码认证过滤器 {@link UsernamePasswordAuthenticationFilter} 之前执行，
        // 确保在进行基于用户名密码的认证之前，先尝试通过 JWT 进行认证。
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}