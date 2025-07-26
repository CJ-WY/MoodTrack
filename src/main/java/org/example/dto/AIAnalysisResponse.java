package org.example.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.model.ai.AnalysisPatterns;
import org.example.model.ai.AnalysisRecommendations;
import org.example.model.ai.AnalysisSummary;
import org.example.model.ai.RiskAssessment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIAnalysisResponse {
    private String reportId;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime generatedAt;

    private AnalysisResult analysisResult;

    private AnalysisMetadata metadata;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnalysisResult {
        private AnalysisSummary summary;
        private AnalysisPatterns patterns;
        private AnalysisRecommendations recommendations;
        private RiskAssessment riskAssessment;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnalysisMetadata {
        private Integer dataPoints;
        private BigDecimal analysisConfidence;
        private BigDecimal apiCost;
    }
}