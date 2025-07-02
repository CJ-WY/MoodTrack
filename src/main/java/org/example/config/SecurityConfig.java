package org.example.config;

import org.example.filter.JwtRequestFilter;
import org.example.service.CustomOAuth2UserService;
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

    /**
     * 定义认证管理器 (AuthenticationManager) 的 Bean。
     * <p>
     * 这是 Spring Security 认证体系的核心，负责处理认证请求。
     * 它会协调 {@link org.springframework.security.core.userdetails.UserDetailsService} 和 {@link PasswordEncoder} 来完成用户认证。
     * </p>
     *
     * @param authenticationConfiguration Spring Security 的认证配置对象，用于获取默认的认证管理器。
     * @return 返回一个 {@link AuthenticationManager} 实例。
     * @throws Exception 如果配置出错，例如无法获取认证管理器。
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
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

        // 将我们自定义的 JWT 过滤器添加到 Spring Security 的过滤器链中。
        // 它会在标准的用户名密码认证过滤器 {@link UsernamePasswordAuthenticationFilter} 之前执行，
        // 确保在进行基于用户名密码的认证之前，先尝试通过 JWT 进行认证。
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}