package org.example.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 社区帖子实体类。
 * <p>
 * 存储用户在社区中发布的帖子内容，包括标题、正文、图片等。
 * </p>
 */
@Data
@Entity
@Table(name = "post")
public class Post {

    /**
     * 帖子的唯一标识符 (主键)。
     * 数据库自动生成。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 帖子的作者。
     * 多对一关系：多个帖子可以由同一个用户发布。
     * 使用 {@link FetchType#LAZY} 延迟加载，以优化性能。
     * `nullable = false` 表示帖子必须关联一个作者。
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    /**
     * 帖子的标题。
     * 不能为空。
     */
    @Column(nullable = false)
    private String title;

    /**
     * 帖子的正文内容。
     * 使用 `columnDefinition = "TEXT"` 以支持存储较长的文本内容。
     */
    @Column(columnDefinition = "TEXT")
    private String content;

    /**
     * 帖子图片的 URL。
     * 如果帖子包含图片，则存储图片在 S3 上的访问链接。
     */
    @Column(name = "image_url")
    private String imageUrl;

    /**
     * 帖子的发布时间。
     * 不能为空。
     */
    @Column(name = "post_time", nullable = false)
    private LocalDateTime postTime;

    /**
     * 帖子下的评论列表。
     * 一对多关系：一个帖子可以有多条评论。
     * `mappedBy` 指向 Comment 实体中的 `post` 字段，表示由 Comment 实体来维护关系。
     * `cascade = CascadeType.ALL` 表示对帖子的操作（如删除）会级联到评论。
     * `orphanRemoval = true` 表示如果评论从列表中移除，则从数据库中删除。
     * 使用 {@link FetchType#LAZY} 延迟加载。
     */
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Comment> comments;

    /**
     * 帖子关联的标签列表。
     * 多对多关系：一个帖子可以有多个标签，一个标签也可以关联到多个帖子。
     * 通过中间表 `post_tag` 进行关联。
     * 使用 {@link FetchType#LAZY} 延迟加载。
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "post_tag",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<Tag> tags;
}
