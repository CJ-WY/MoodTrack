package org.example.model;

import jakarta.persistence.*;
import lombok.Data;

/**
 * 情绪群体标签实体类。
 * <p>
 * 用于存储不同的情绪或兴趣标签，例如“焦虑群体”、“孤独型”等。
 * 这些标签可以用于对用户情绪分析结果的分类，以及对社区帖子的归类。
 * </p>
 */
@Data
@Entity
@Table(name = "tag")
public class Tag {

    /**
     * 标签的唯一标识符 (主键)。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 标签的名称。
     * 必须唯一，例如“焦虑群体”。
     */
    @Column(nullable = false, unique = true)
    private String name;
}