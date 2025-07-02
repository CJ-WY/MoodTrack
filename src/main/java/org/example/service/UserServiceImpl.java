package org.example.service;

import org.example.model.User;
import org.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 用户服务实现类。
 * <p>
 * 实现了 {@link UserService} 接口，处理用户注册和查找的业务逻辑。
 * </p>
 */
@Service
public class UserServiceImpl implements UserService {

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
     * @param user 包含用户名、邮箱、密码等信息的 User 对象。
     * @return 注册成功后保存到数据库的 User 对象。
     */
    @Override
    public User register(User user) {
        // 对用户密码进行加密
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // 设置用户注册时间为当前时间
        user.setRegistrationDate(LocalDateTime.now());
        // 保存用户到数据库
        return userRepository.save(user);
    }

    /**
     * 根据用户名查找用户。
     *
     * @param username 用户名。
     * @return 查找到的 User 对象，如果不存在则返回 null。
     */
    @Override
    public User findByUsername(String username) {
        // 从数据库中根据用户名查找用户，如果不存在则返回 null
        return userRepository.findByUsername(username).orElse(null);
    }
}