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
public class AnalysisPatterns {
    private WeeklyPattern weeklyPattern;
    private DailyPattern dailyPattern;
    private TriggerAnalysis triggers;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeeklyPattern {
        private List<String> bestDays;
        private List<String> challengingDays;
        private BigDecimal volatilityIndex;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyPattern {
        private BigDecimal morningAverage;
        private BigDecimal eveningAverage;
        private List<String> peakHours;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TriggerAnalysis {
        private List<Trigger> positive;
        private List<Trigger> negative;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Trigger {
        private String factor;
        private Integer frequency;
        private BigDecimal impact;
    }
}