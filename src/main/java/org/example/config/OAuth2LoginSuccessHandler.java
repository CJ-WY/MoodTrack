package org.example.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.service.CustomOAuth2UserService;
import org.example.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * OAuth2 登录成功处理器。
 * <p>
 * 当用户通过 OAuth2 (如 Google) 成功登录后，此处理器会被调用。
 * 它的主要职责是为成功登录的用户生成一个 JWT，并将其返回给前端。
 * </p>
 */
@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2LoginSuccessHandler.class);

    /**
     * 注入 JWT 工具类，用于生成 Token。
     */
    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 在 OAuth2 认证成功后被调用。
     * <p>
     * 此方法会从 {@link Authentication} 对象中提取用户详情，
     * 然后使用 {@link JwtUtil} 生成一个 JWT，并重定向前端到指定 URL，将 JWT 作为参数传递。
     * </p>
     *
     * @param request        HTTP 请求对象。
     * @param response       HTTP 响应对象。
     * @param authentication 认证对象，包含了成功认证的用户信息。
     * @throws IOException      如果发生 IO 异常。
     * @throws ServletException 如果发生 Servlet 异常。
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        logger.info("OAuth2 登录成功，用户: {}", authentication.getName());

        // 从认证对象中获取用户详情
        // 这里的 principal 可能是 CustomOAuth2User 类型，需要进行类型转换
        UserDetails userDetails;
        if (authentication.getPrincipal() instanceof CustomOAuth2UserService.CustomOAuth2User) {
            userDetails = ((CustomOAuth2UserService.CustomOAuth2User) authentication.getPrincipal()).getUser();
        } else if (authentication.getPrincipal() instanceof UserDetails) {
            userDetails = (UserDetails) authentication.getPrincipal();
        } else {
            logger.error("无法从认证对象中获取 UserDetails 类型。Principal 类型: {}", authentication.getPrincipal().getClass().getName());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "认证成功后无法处理用户信息。");
            return;
        }

        // 生成我们自己的 JWT
        final String jwt = jwtUtil.generateToken(userDetails);
        logger.info("为用户 {} 生成 JWT: {}", userDetails.getUsername(), jwt);

        // 将 JWT 返回给前端
        // 通常的做法是重定向到前端的某个 URL，并将 JWT 作为查询参数或 Fragment 传递
        // 例如：http://localhost:3000/oauth2/redirect?token=<JWT>
        // 前端收到后，从 URL 中提取 JWT 并保存。
        // 注意：这里的重定向 URL 应该从配置中获取，或者由前端在发起 OAuth2 登录时提供。
        // 为了简化，这里硬编码了一个示例，实际应用中应更灵活。
        String redirectUrl = "http://localhost:3000/oauth2/redirect?token=" + jwt; // 假设前端重定向地址
        response.sendRedirect(redirectUrl);
    }
}