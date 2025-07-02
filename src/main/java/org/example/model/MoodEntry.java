package org.example.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户每日情绪记录实体类。
 * <p>
 * 存储用户每天提交的情绪数据，包括心情描述、压力指数和体感状态等。
 * </p>
 */
@Data
@Entity
@Table(name = "mood_entry")
public class MoodEntry {

    /**
     * 情绪记录的唯一标识符 (主键)。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 记录所属的用户。
     * 多对一关系：多个情绪记录可以属于同一个用户。
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 用户对当前心情的文字描述。
     * 例如：“今天感觉很平静”、“有点沮丧”。
     */
    @Column(name = "mood_description", nullable = false)
    private String moodDescription;

    /**
     * 用户的压力指数，通常是一个从 1 到 10 的整数。
     * 1 表示压力很小，10 表示压力很大。
     */
    @Column(name = "stress_level", nullable = false)
    private Integer stressLevel;

    /**
     * 用户的体感状态描述。
     * 例如：“身体疲惫”、“精力充沛”、“头痛”。
     */
    @Column(name = "physical_state")
    private String physicalState;

    /**
     * 情绪记录的时间戳。
     */
    @Column(name = "record_time", nullable = false)
    private LocalDateTime recordTime;
}