package org.example.filter;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.service.MyUserDetailsService;
import org.example.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 请求过滤器。
 * <p>
 * 这个过滤器在每个请求到达受保护的端点之前执行一次。
 * 它的核心任务是检查请求头中是否包含有效的 JWT (JSON Web Token)，
 * 如果有，就解析它并设置 Spring Security 的安全上下文，从而认证用户。
 * </p>
 */
@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtRequestFilter.class);

    /**
     * 注入自定义的用户详情服务，用于根据用户名加载用户信息。
     */
    @Autowired
    private MyUserDetailsService myUserDetailsService;

    /**
     * 注入 JWT 工具类，用于生成、解析和验证 Token。
     */
    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 对每一个 HTTP 请求进行过滤处理的核心方法。
     * <p>
     * 1. 从请求头中获取 Authorization 字段。
     * 2. 检查 Token 是否以 "Bearer " 开头，并提取 JWT 字符串。
     * 3. 尝试解析 JWT，提取用户名。
     * 4. 如果用户名有效且当前安全上下文中没有认证信息，则加载用户详情。
     * 5. 验证 Token 的有效性（用户名匹配且未过期）。
     * 6. 如果 Token 有效，则创建认证通过的 Token 并设置到 Spring Security 的安全上下文中。
     * 7. 将请求传递给过滤器链中的下一个过滤器。
     * </p>
     *
     * @param request     HTTP 请求对象。
     * @param response    HTTP 响应对象。
     * @param filterChain 过滤器链，用于将请求传递给下一个过滤器。
     * @throws ServletException 如果发生 Servlet 异常。
     * @throws IOException      如果发生 IO 异常。
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        // 1. 检查 Authorization Header 是否存在并且以 "Bearer " 开头
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7); // 截取 "Bearer " 后面的 Token 字符串
            try {
                username = jwtUtil.extractUsername(jwt); // 从 Token 中解析出用户名
            } catch (ExpiredJwtException e) {
                logger.warn("JWT Token 已过期: {}", e.getMessage());
                // 可以选择在这里设置 HTTP 状态码 401 Unauthorized
                // response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                // return;
            } catch (Exception e) {
                logger.error("解析 JWT Token 时发生错误: {}", e.getMessage(), e);
                // 可以选择在这里设置 HTTP 状态码 400 Bad Request 或 401 Unauthorized
                // response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                // return;
            }
        }

        // 2. 如果成功获取到用户名，并且当前安全上下文中没有认证信息 (避免重复认证)
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // 3. 根据用户名加载用户的详细信息 (包括权限等)
            UserDetails userDetails = this.myUserDetailsService.loadUserByUsername(username);

            // 4. 验证 Token 是否有效 (用户名匹配且未过期)
            if (jwtUtil.validateToken(jwt, userDetails)) {
                // 5. 如果 Token 有效，则创建一个认证通过的 Token
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                // 6. 将请求的详细信息 (如 IP 地址、Session ID) 设置到认证 Token 中
                usernamePasswordAuthenticationToken
                        .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                // 7. 将这个认证通过的 Token 设置到 Spring Security 的安全上下文中
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                logger.debug("用户 '{}' 认证成功并设置安全上下文。", username);
            }
        }
        // 8. 将请求传递给过滤器链中的下一个过滤器
        filterChain.doFilter(request, response);
    }
}