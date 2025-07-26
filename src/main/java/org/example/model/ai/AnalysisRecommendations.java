package org.example.model.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisRecommendations {
    private List<Recommendation> immediate;
    private List<Recommendation> shortTerm;
    private List<Recommendation> longTerm;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Recommendation {
        private String title;
        private String description;
        private String priority;
        private String estimatedImpact;
    }
}