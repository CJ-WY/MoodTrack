package org.example.repository;

import org.example.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 评论内容的数据仓库接口。
 * <p>
 * 继承自 {@link JpaRepository}，提供了对 {@link Comment} 实体进行数据库操作的基础方法。
 * </p>
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
}
