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

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7); // 截取 "Bearer " 后面的 Token 字符串
            try {
                username = jwtUtil.extractUsername(jwt); // 从 Token 中解析出用户名
            } catch (ExpiredJwtException e) {
                logger.warn("JWT Token has expired: {}", e.getMessage());
            } catch (Exception e) {
                logger.error("Error parsing JWT Token: {}", e.getMessage());
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.myUserDetailsService.loadUserByUsername(username);

            if (jwtUtil.validateToken(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                usernamePasswordAuthenticationToken
                        .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                logger.debug("User '{}' authenticated successfully.", username);
            }
        }
        filterChain.doFilter(request, response);
    }
}
