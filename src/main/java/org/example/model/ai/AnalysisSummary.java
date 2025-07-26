package org.example.model.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisSummary {
    private String overallTrend;
    private BigDecimal averageScore;
    private List<String> keyInsights;
    private String urgencyLevel;
}