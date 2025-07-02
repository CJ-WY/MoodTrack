package org.example.repository;

import org.example.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 情绪群体标签的数据仓库接口。
 * <p>
 * 继承自 {@link JpaRepository}，提供了基础的 CRUD (创建、读取、更新、删除) 功能。
 * </p>
 */
@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    /**
     * 根据标签名称查找标签。
     * <p>
     * Spring Data JPA 会根据方法名自动生成查询语句。
     * 这个方法用于在创建新标签前，检查数据库中是否已存在同名标签。
     * </p>
     *
     * @param name 标签的名称。
     * @return 一个包含查找到的 {@link Tag} 实体的 {@link Optional} 对象，如果找不到则为空。
     */
    Optional<Tag> findByName(String name);
}
