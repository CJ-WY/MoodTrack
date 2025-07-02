package org.example.config;

import org.example.filter.JwtRequestFilter; // 更新导入路径
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 配置类
 * <p>
 * 负责整个应用的安全认证和授权设置。
 * </p>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * 注入自定义的 JWT 请求过滤器。
     */
    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    /**
     * 定义一个密码加密器 (PasswordEncoder) 的 Bean。
     * <p>
     * 使用 BCrypt 强哈希函数对密码进行加密，增强密码存储的安全性。
     * </p>
     *
     * @return 返回一个 BCryptPasswordEncoder 实例。
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 定义认证管理器 (AuthenticationManager) 的 Bean。
     * <p>
     * 这是 Spring Security 认证体系的核心，负责处理认证请求。
     * </p>
     *
     * @param authenticationConfiguration Spring Security 的认证配置对象。
     * @return 返回一个 AuthenticationManager 实例。
     * @throws Exception 如果配置出错。
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * 配置安全过滤链 (SecurityFilterChain)。
     * <p>
     * 这是定义所有 HTTP 请求安全策略的核心位置。
     * </p>
     *
     * @param http HttpSecurity 对象，用于构建安全策略。
     * @return 返回配置好的 SecurityFilterChain 实例。
     * @throws Exception 如果配置出错。
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 禁用 CSRF (跨站请求伪造) 保护，因为我们使用 JWT，是无状态的，所以不需要 CSRF 保护。
                .csrf(csrf -> csrf.disable())
                // 配置请求授权规则
                .authorizeHttpRequests(authorize -> authorize
                        // 对以下路径的请求允许匿名访问 (无需认证)
                        // 主要包括用户认证(注册/登录)接口和 API 文档(Swagger)相关路径。
                        .requestMatchers("/api/auth/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // 除了上面明确放行的路径，所有其他请求都必须经过认证。
                        .anyRequest().authenticated()
                )
                // 配置会话管理策略
                .sessionManagement(session -> session
                        // 设置会话创建策略为 STATELESS (无状态)。
                        // 这意味着服务器不会创建或使用 HttpSession，每次请求都依赖于 JWT 进行认证。
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        // 将我们自定义的 JWT 过滤器添加到 Spring Security 的过滤器链中。
        // 它会在标准的用户名密码认证过滤器之前执行。
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
