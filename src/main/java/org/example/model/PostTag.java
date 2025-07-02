package org.example.model;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;

/**
 * 帖子与标签关联关系实体类 (多对多中间表)。
 * <p>
 * 用于映射 {@link Post} 和 {@link Tag} 之间的多对多关系。
 * 这是一个复合主键的实体，需要实现 {@link Serializable} 接口。
 * </p>
 */
@Data
@Entity
@Table(name = "post_tag")
@IdClass(PostTag.class) // 指定复合主键类
public class PostTag implements Serializable {

    /**
     * 帖子的引用，作为复合主键的一部分。
     * 多对一关系：多个关联记录指向同一个帖子。
     * 使用 {@link FetchType#LAZY} 延迟加载。
     */
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    /**
     * 标签的引用，作为复合主键的一部分。
     * 多对一关系：多个关联记录指向同一个标签。
     * 使用 {@link FetchType#LAZY} 延迟加载。
     */
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id")
    private Tag tag;
}
