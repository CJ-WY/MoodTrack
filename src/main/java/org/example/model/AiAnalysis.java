package org.example.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Digits;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.model.ai.AnalysisPatterns;
import org.example.model.ai.AnalysisRecommendations;
import org.example.model.ai.AnalysisSummary;
import org.example.model.ai.RiskAssessment;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "ai_analysis")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "report_id", unique = true, nullable = false)
    private String reportId;

    @Enumerated(EnumType.STRING)
    @Column(name = "analysis_type")
    @Builder.Default
    private AnalysisType analysisType = AnalysisType.WEEKLY;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "summary_data", nullable = false, columnDefinition = "jsonb")
    private AnalysisSummary summaryData;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "patterns_data", nullable = false, columnDefinition = "jsonb")
    private AnalysisPatterns patternsData;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "recommendations_data", nullable = false, columnDefinition = "jsonb")
    private AnalysisRecommendations recommendationsData;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "risk_assessment_data", nullable = false, columnDefinition = "jsonb")
    private RiskAssessment riskAssessmentData;

    @Column(name = "data_points", nullable = false)
    private Integer dataPoints;

    @Column(name = "confidence_score", nullable = false)
    @Digits(integer = 1, fraction = 2)
    private BigDecimal confidenceScore;

    @Column(name = "api_cost")
    @Digits(integer = 4, fraction = 4)
    private BigDecimal apiCost;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}