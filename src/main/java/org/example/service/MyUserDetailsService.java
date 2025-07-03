package org.example.service;

import org.example.model.User;
import org.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 * 用户详细信息服务实现类。
 * <p>
 * 实现了 Spring Security 的 {@link UserDetailsService} 接口，
 * 用于从数据库加载用户认证信息。
 * </p>
 */
@Service
public class MyUserDetailsService implements UserDetailsService {

    /**
     * 用户数据仓库，用于从数据库查询用户信息。
     */
    @Autowired
    private UserRepository userRepository;

    /**
     * 根据用户名加载用户详细信息。
     * <p>
     * 这是 Spring Security 认证流程中，用于获取用户凭证和权限的核心方法。
     * </p>
     *
     * @param username 尝试认证的用户名。
     * @return 包含用户认证信息的 {@link UserDetails} 对象。
     * @throws UsernameNotFoundException 如果数据库中找不到对应的用户。
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 尝试通过用户名查找用户
        User user = userRepository.findByUsername(username).orElse(null);

        // 如果未找到，则尝试通过邮箱查找
        if (user == null) {
            user = userRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException("未找到用户: " + username));
        }

        // 返回 Spring Security 框架所需的 UserDetails 对象
        // 这里我们只使用了用户名和加密后的密码，没有额外的权限信息 (空列表)
        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), new ArrayList<>());
    }
}
