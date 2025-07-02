package org.example.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

/**
 * Gemini 分析结果实体类
 * <p>
 * 存储由 Gemini API 对单次情绪记录分析后得出的所有信息，
 * 并通过多对多关系关联到具体的标签(Tag)。
 * </p>
 */
@Data
@Entity
@Table(name = "ai_analysis")
public class AiAnalysis {

    /**
     * 唯一标识符 (主键)。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 与本次分析关联的情绪记录。
     * 使用一对一关系，确保一次情绪记录只对应一次分析。
     */
    @OneToOne(fetch = FetchType.LAZY) // 使用延迟加载优化性能
    @JoinColumn(name = "mood_entry_id", nullable = false, unique = true)
    private MoodEntry moodEntry;

    /**
     * AI 生成的对用户情绪的详细文本分析。
     */
    @Column(name = "analysis_text", columnDefinition = "TEXT")
    private String analysisText;

    /**
     * AI 针对用户当前状态提供的具体建议。
     */
    @Column(columnDefinition = "TEXT")
    private String suggestion;

    /**
     * AI 分析后匹配到的标签列表。
     * 使用多对多关系，一个分析结果可以有多个标签，一个标签也可以对应多个分析结果。
     * 中间表 `ai_analysis_tag` 会被自动创建，用于存储关联关系。
     */
    @ManyToMany(fetch = FetchType.EAGER) // 使用即时加载，因为通常获取分析时就需要标签
    @JoinTable(
            name = "ai_analysis_tag",
            joinColumns = @JoinColumn(name = "ai_analysis_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<Tag> matchedTags;
}