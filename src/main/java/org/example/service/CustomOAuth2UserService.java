package org.example.service;

import org.example.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * 自定义 OAuth2 用户服务。
 * <p>
 * 继承自 {@link DefaultOAuth2UserService}，用于处理从 OAuth2 提供商（如 Google）获取到的用户信息。
 * 它的主要职责是将外部 OAuth2 用户信息映射到我们应用内部的 {@link User} 实体，
 * 并处理用户的注册或更新逻辑。
 * </p>
 */
@Service
@Transactional
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final Logger logger = LoggerFactory.getLogger(CustomOAuth2UserService.class);

    /**
     * 注入用户服务，用于查找、保存或更新用户。
     */
    @Autowired
    private UserService userService;

    /**
     * 加载 OAuth2 用户。
     * <p>
     * 当用户通过 OAuth2 提供商（如 Google）成功认证后，Spring Security 会调用此方法。
     * </p>
     *
     * @param userRequest 包含 OAuth2 认证请求信息的对象。
     * @return 包含用户详细信息的 {@link OAuth2User} 对象。
     * @throws OAuth2AuthenticationException 如果加载用户失败。
     */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. 调用父类方法，获取默认的 OAuth2User 对象，其中包含了从 Google 获取到的用户信息
        OAuth2User oauth2User = super.loadUser(userRequest);

        // 2. 从 OAuth2User 中提取 Google 用户的关键信息
        String googleId = oauth2User.getName(); // Google 的 sub 字段通常作为 name
        String email = oauth2User.getAttribute("email");
        String username = oauth2User.getAttribute("name"); // Google 提供的显示名称

        logger.info("OAuth2 登录用户：Google ID - {}, Email - {}, Username - {}", googleId, email, username);

        // 3. 根据 Google ID 或 Email 查找或创建本地用户
        User user = userService.findByGoogleId(googleId);

        if (user == null) {
            // 如果没有找到匹配的 Google ID，尝试通过邮箱查找
            user = userService.findByEmail(email);
            if (user == null) {
                // 如果用户不存在，则创建新用户
                logger.info("创建新用户：Email - {}", email);
                user = new User();
                user.setGoogleId(googleId);
                user.setEmail(email);
                user.setUsername(username != null ? username : email); // 如果 Google 没有提供 name，则使用 email 作为用户名
                user.setPassword(""); // OAuth2 登录的用户，密码可以为空或设置一个默认值
                user.setRegistrationDate(LocalDateTime.now());
                user = userService.save(user);
            } else {
                // 如果通过邮箱找到了用户，但没有 Google ID，则关联 Google ID
                logger.info("关联现有用户 {} 到 Google ID {}", user.getUsername(), googleId);
                user.setGoogleId(googleId);
                user = userService.save(user);
            }
        } else {
            // 如果通过 Google ID 找到了用户，更新其信息（例如用户名或邮箱可能已更新）
            logger.info("更新现有用户 {} 的信息", user.getUsername());
            user.setEmail(email);
            user.setUsername(username != null ? username : email);
            user = userService.save(user);
        }

        // 4. 返回一个包含我们本地用户信息的 OAuth2User 对象
        // Spring Security 会使用这个对象来构建认证信息
        return new CustomOAuth2User(user, oauth2User.getAttributes());
    }

    /**
     * 内部类，用于封装我们自己的 User 实体和 OAuth2User 的属性。
     * 这样 Spring Security 就可以继续使用 OAuth2User 的接口，
     * 但底层数据是我们自己的 User 实体。
     */
    public static class CustomOAuth2User implements OAuth2User {
        private final User user;
        private final Map<String, Object> attributes;

        public CustomOAuth2User(User user, Map<String, Object> attributes) {
            this.user = user;
            this.attributes = attributes;
        }

        @Override
        public Map<String, Object> getAttributes() {
            return attributes;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            // 这里可以根据用户角色返回权限，目前返回空列表
            return new ArrayList<>();
        }

        @Override
        public String getName() {
            // 返回 Google 提供的唯一标识符 (sub)
            return user.getGoogleId();
        }

        public User getUser() {
            return user;
        }
    }
}