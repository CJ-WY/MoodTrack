package org.example.service;

import org.example.model.User;
import org.example.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 用户服务实现类。
 * <p>
 * 实现了 {@link UserService} 接口，处理用户注册和查找的业务逻辑。
 * </p>
 */
@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    /**
     * 用户数据仓库，用于与数据库进行交互。
     */
    @Autowired
    private UserRepository userRepository;

    /**
     * 密码编码器，用于对用户密码进行加密。
     */
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 注册新用户。
     * <p>
     * 在保存用户之前，会对密码进行 BCrypt 加密，并设置注册时间为当前时间。
     * </p>
     *
     * @param user 包含用户名、邮箱、密码等信息的 {@link User} 对象。
     * @return 注册成功后保存到数据库的 {@link User} 对象。
     * @throws RuntimeException 如果数据库操作失败。
     */
    @Override
    @Transactional // 确保数据库操作的原子性
    public User register(User user) {
        try {
            // 对用户密码进行加密
            // 注意：对于 OAuth2 登录的用户，密码可能为空或设置为一个默认值
            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            } else {
                // 如果是 OAuth2 注册，可以设置一个随机或默认的加密密码
                user.setPassword(passwordEncoder.encode("default_oauth2_password")); // 或者其他策略
            }
            // 设置用户注册时间为当前时间
            user.setRegistrationDate(LocalDateTime.now());
            // 保存用户到数据库
            return userRepository.save(user);
        } catch (Exception e) {
            throw new RuntimeException("用户注册失败。", e);
        }
    }

    /**
     * 根据用户名查找用户。
     *
     * @param username 用户名。
     * @return 查找到的 {@link User} 对象，如果不存在则返回 null。
     * @throws RuntimeException 如果数据库操作失败。
     */
    @Override
    public User findByUsername(String username) {
        try {
            // 从数据库中根据用户名查找用户，如果不存在则返回 null
            return userRepository.findByUsername(username).orElse(null);
        } catch (Exception e) {
            throw new RuntimeException("根据用户名查找用户失败。", e);
        }
    }

    /**
     * 根据 Google ID 查找用户。
     *
     * @param googleId Google 提供的用户唯一 ID。
     * @return 查找到的 {@link User} 对象，如果不存在则返回 null。
     * @throws RuntimeException 如果数据库操作失败。
     */
    @Override
    public User findByGoogleId(String googleId) {
        try {
            return userRepository.findByGoogleId(googleId).orElse(null);
        } catch (Exception e) {
            throw new RuntimeException("根据 Google ID 查找用户失败。", e);
        }
    }

    /**
     * 根据邮箱查找用户。
     *
     * @param email 用户邮箱。
     * @return 查找到的 {@link User} 对象，如果不存在则返回 null。
     * @throws RuntimeException 如果数据库操作失败。
     */
    @Override
    public User findByEmail(String email) {
        try {
            return userRepository.findByEmail(email).orElse(null);
        } catch (Exception e) {
            throw new RuntimeException("根据邮箱查找用户失败。", e);
        }
    }

    /**
     * 保存或更新用户。
     * <p>
     * 此方法用于保存新用户或更新现有用户的信息，特别是在 OAuth2 登录流程中。
     * </p>
     *
     * @param user 需要保存或更新的 {@link User} 对象。
     * @return 保存或更新后的 {@link User} 对象。
     * @throws RuntimeException 如果数据库操作失败。
     */
    @Override
    @Transactional
    public User save(User user) {
        try {
            logger.info("尝试保存或更新用户: {}", user.getEmail());
            User savedUser = userRepository.save(user);
            logger.info("用户保存成功，ID: {}", savedUser.getId());
            return savedUser;
        } catch (Exception e) {
            logger.error("保存或更新用户失败: {}", user.getEmail(), e);
            throw new RuntimeException("保存或更新用户失败。", e);
        }
    }
}