package org.example.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户信息实体类。
 * <p>
 * 存储平台用户的基本信息，如用户名、邮箱、密码和注册时间。
 * </p>
 */
@Data
@Entity
@Table(name = "users") // 将表名设置为 "users" 以避免与某些数据库系统中的保留关键字冲突
public class User {

    /**
     * 用户的唯一标识符 (主键)。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户名。
     * 必须唯一，用于登录和身份识别。
     */
    @Column(nullable = false, unique = true)
    private String username;

    /**
     * 用户邮箱。
     * 必须唯一，可用于找回密码或通知。
     */
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * 用户密码。
     * 存储的是经过加密 (哈希) 后的密码，而不是明文。
     */
    @Column(nullable = false)
    private String password;

    /**
     * 用户注册的时间。
     */
    @Column(name = "registration_date", nullable = false)
    private LocalDateTime registrationDate;
}