package org.example.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论内容实体类。
 * <p>
 * 存储用户对帖子或对其他评论的回复内容。
 * 支持多级嵌套评论。
 * </p>
 */
@Data
@Entity
@Table(name = "comment")
public class Comment {

    /**
     * 评论的唯一标识符 (主键)。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 评论所属的帖子。
     * 多对一关系：多条评论可以属于同一个帖子。
     * 使用 {@link FetchType#LAZY} 延迟加载，以优化性能，避免不必要的关联查询。
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    /**
     * 发表评论的用户。
     * 多对一关系：多个评论可以由同一个用户发表。
     * 使用 {@link FetchType#LAZY} 延迟加载。
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commenter_id", nullable = false)
    private User commenter;

    /**
     * 评论的具体内容。
     * 使用 `columnDefinition = "TEXT"` 以支持存储较长的文本内容。
     */
    @Column(columnDefinition = "TEXT")
    private String content;

    /**
     * 评论发布的时间。
     * 不能为空。
     */
    @Column(name = "comment_time", nullable = false)
    private LocalDateTime commentTime;

    /**
     * 父评论。
     * 用于实现嵌套评论，如果此评论是对另一个评论的回复，则指向父评论。
     * 如果是直接对帖子的评论，则此字段为 null。
     * 使用 {@link FetchType#LAZY} 延迟加载。
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private Comment parentComment;

    /**
     * 子评论列表。
     * 一对多关系：一个评论可以有多个回复 (子评论)。
     * `mappedBy` 指向子评论中的 `parentComment` 字段，表示由 Comment 实体来维护关系。
     * `cascade = CascadeType.ALL` 表示对父评论的操作（如删除）会级联到子评论。
     * `orphanRemoval = true` 表示如果子评论从列表中移除，则从数据库中删除。
     * 使用 {@link FetchType#LAZY} 延迟加载。
     */
    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Comment> replies;
}
