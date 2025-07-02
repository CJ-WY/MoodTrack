package org.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码编码器配置类。
 * <p>
 * 负责定义和提供 {@link PasswordEncoder} 的 Spring Bean。
 * 将其独立出来有助于解决 Spring Security 配置中的循环依赖问题，
 * 确保 {@link PasswordEncoder} 在其他依赖它的 Bean 之前被正确初始化。
 * </p>
 */
@Configuration
public class PasswordEncoderConfig {

    /**
     * 定义一个密码加密器 (PasswordEncoder) 的 Bean。
     * <p>
     * 使用 BCrypt 强哈希函数对密码进行加密，增强密码存储的安全性。
     * Spring Security 在认证时会使用此编码器来验证用户输入的密码。
     * </p>
     *
     * @return 返回一个 {@link BCryptPasswordEncoder} 实例。
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}