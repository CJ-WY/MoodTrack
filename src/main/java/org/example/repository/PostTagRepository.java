package org.example.repository;

import org.example.model.PostTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 帖子与标签关联关系的数据仓库接口。
 * <p>
 * 继承自 {@link JpaRepository}，提供了对 {@link PostTag} 实体进行数据库操作的基础方法。
 * </p>
 */
@Repository
public interface PostTagRepository extends JpaRepository<PostTag, Long> {
}
