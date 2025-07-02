package org.example.repository;

import org.example.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 社区帖子的数据仓库接口。
 * <p>
 * 继承自 JpaRepository，提供了基础的 CRUD 功能。
 * </p>
 */
@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    /**
     * 根据一个或多个标签名称，查找所有至少包含其中一个标签的帖子。
     * <p>
     * 这个方法使用了自定义的 JPQL (Java Persistence Query Language) 查询。
     * 1. `SELECT DISTINCT p`: 查询并返回唯一的 Post 实体 (p)。DISTINCT 防止因一个帖子匹配多个标签而重复出现。
     * 2. `FROM Post p JOIN p.tags t`: 从 Post 实体 (p) 开始，并连接 (JOIN) 到它的 `tags` 集合 (t)。
     * 3. `WHERE t.name IN :tagNames`: 筛选条件，只选择那些连接到的标签 (t) 的名称 (name) 存在于传入的 `tagNames` 列表中的帖子。
     * </p>
     *
     * @param tagNames 一个包含多个标签名称的列表。
     * @return 一个包含所有匹配帖子的列表。
     */
    @Query("SELECT DISTINCT p FROM Post p JOIN p.tags t WHERE t.name IN :tagNames")
    List<Post> findPostsByTagNames(@Param("tagNames") List<String> tagNames);
}