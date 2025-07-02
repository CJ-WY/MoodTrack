package org.example.repository;

import org.example.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户信息的数据仓库接口。
 * <p>
 * 继承自 JpaRepository，提供了对 {@link User} 实体进行数据库操作的基础方法。
 * </p>
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 根据用户名查找用户。
     * <p>
     * Spring Data JPA 会根据方法名自动生成查询语句。
     * </p>
     *
     * @param username 用户名。
     * @return 一个包含查找到的 User 实体的 Optional 对象，如果找不到则为空。
     */
    Optional<User> findByUsername(String username);
}