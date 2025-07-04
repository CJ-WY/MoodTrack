package org.example.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.service.CustomOAuth2UserService;
import org.example.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.example.service.MyUserDetailsService;
import org.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Autowired
    private MyUserDetailsService myUserDetailsService;

    @Autowired
    private UserService userService;

    @Value("${app.oauth2.redirect-uri.frontend}")
    private String frontendRedirectUri;

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
        UserDetails userDetails;
        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomOAuth2UserService.CustomOAuth2User) {
            // 如果是自定义的 CustomOAuth2User，直接获取其内部的 UserDetails
            userDetails = ((CustomOAuth2UserService.CustomOAuth2User) principal).getUser();
        } else if (principal instanceof org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser) {
            // 如果是 DefaultOidcUser (通常在 OpenID Connect 流程中)，通过 email 加载 UserDetails
            org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser oidcUser =
                    (org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser) principal;
            String email = oidcUser.getEmail();
            if (email == null) {
                logger.error("DefaultOidcUser 中未找到 email 属性。");
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "认证成功后无法处理用户信息：缺少邮箱。");
                return;
            }
            // 使用 UserService 根据 email 加载用户详情
            org.example.model.User user = userService.findByEmail(email);
            if (user == null) {
                logger.error("通过邮箱 {} 未找到用户，但在 CustomOAuth2UserService 中应该已处理。", email);
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "认证成功后无法处理用户信息：用户不存在。");
                return;
            }
            // 将我们自己的 User 对象包装成 Spring Security 的 UserDetails
            userDetails = new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), user.getAuthorities());
        } else if (principal instanceof UserDetails) {
            // 如果是普通的 UserDetails (例如，通过用户名密码登录)
            userDetails = (UserDetails) principal;
        } else {
            logger.error("无法从认证对象中获取 UserDetails 类型。Principal 类型: {}", principal.getClass().getName());
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
        String redirectUrl = frontendRedirectUri + "?token=" + jwt;
        response.sendRedirect(redirectUrl);
    }
}