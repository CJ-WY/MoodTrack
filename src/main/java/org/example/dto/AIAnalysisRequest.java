package org.example.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AIAnalysisRequest {

    @Builder.Default
    private String analysisType = "weekly";

    @Builder.Default
    private DateRange dateRange = new DateRange();

    @Builder.Default
    private AnalysisPreferences preferences = new AnalysisPreferences();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DateRange {
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate startDate;
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate endDate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnalysisPreferences {
        @Builder.Default
        private String language = "zh-CN";
        @Builder.Default
        private String depth = "detailed";
        @Builder.Default
        private List<String> focusAreas = new ArrayList<>();
    }
}