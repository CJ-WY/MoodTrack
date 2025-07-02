package org.example.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

/**
 * 用户信息实体类。
 * <p>
 * 存储平台用户的基本信息，如用户名、邮箱、密码和注册时间。
 * 扩展支持 Google OAuth2 登录。
 * 同时实现 {@link UserDetails} 接口，以便 Spring Security 可以直接使用此实体进行认证和授权。
 * </p>
 */
@Data
@Entity
@Table(name = "users") // 将表名设置为 "users" 以避免与某些数据库系统中的保留关键字冲突
public class User implements UserDetails {

    /**
     * 用户的唯一标识符 (主键)。
     * 数据库自动生成。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户名。
     * 必须唯一，用于登录和身份识别。
     * 不能为空。
     */
    @Column(nullable = false, unique = true)
    private String username;

    /**
     * 用户邮箱。
     * 必须唯一，可用于找回密码或通知。
     * 不能为空。
     */
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * 用户密码。
     * 存储的是经过加密 (哈希) 后的密码，而不是明文。
     * 不能为空。
     * 注意：对于 OAuth2 登录的用户，此字段可以为空或设置为一个默认值。
     */
    @Column(nullable = false)
    private String password;

    /**
     * 用户注册的时间。
     * 不能为空。
     */
    @Column(name = "registration_date", nullable = false)
    private LocalDateTime registrationDate;

    /**
     * Google 提供的用户唯一 ID。
     * 用于关联通过 Google OAuth2 登录的用户。
     * 如果用户通过传统方式注册，此字段为 null。
     */
    @Column(name = "google_id", unique = true)
    private String googleId;

    // --- UserDetails 接口实现 --- //

    /**
     * 返回授予用户的权限。
     * <p>
     * 这里简化处理，返回一个空集合。如果需要基于角色的权限控制，可以在这里返回用户的角色。
     * </p>
     *
     * @return 权限集合。
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList(); // 暂时返回空列表，如果需要角色/权限，可以在这里实现
    }

    /**
     * 返回用于验证用户身份的密码。
     *
     * @return 密码。
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * 返回用于验证用户身份的用户名。
     *
     * @return 用户名。
     */
    @Override
    public String getUsername() {
        return username;
    }

    /**
     * 指示用户的帐户是否已过期。
     * <p>
     * true 表示帐户有效（未过期）。
     * </p>
     *
     * @return 始终返回 true。
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * 指示用户是否被锁定或解锁。
     * <p>
     * true 表示帐户未锁定。
     * </p>
     *
     * @return 始终返回 true。
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * 指示用户的凭据（密码）是否已过期。
     * <p>
     * true 表示凭据有效（未过期）。
     * </p>
     *
     * @return 始终返回 true。
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * 指示用户是否已启用或禁用。
     * <p>
     * true 表示用户已启用。
     * </p>
     *
     * @return 始终返回 true。
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}
