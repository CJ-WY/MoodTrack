package org.example.service;

import org.example.model.User;

/**
 * 用户服务接口。
 * <p>
 * 定义了与用户相关的业务逻辑操作，如注册和查找用户。
 * </p>
 */
public interface UserService {

    /**
     * 注册新用户。
     * <p>
     * 接收用户注册信息，对密码进行加密，并保存到数据库。
     * </p>
     *
     * @param user 包含用户名、邮箱、密码等信息的 {@link User} 对象。
     * @return 注册成功后保存到数据库的 {@link User} 对象。
     */
    User register(User user);

    /**
     * 根据用户名查找用户。
     *
     * @param username 用户名。
     * @return 查找到的 {@link User} 对象，如果不存在则返回 null。
     */
    User findByUsername(String username);

    

    /**
     * 根据邮箱查找用户。
     *
     * @param email 用户邮箱。
     * @return 查找到的 {@link User} 对象，如果不存在则返回 null。
     */
    User findByEmail(String email);

    /**
     * 保存或更新用户。
     *
     * @param user 需要保存或更新的 {@link User} 对象。
     * @return 保存或更新后的 {@link User} 对象。
     */
    User save(User user);
}