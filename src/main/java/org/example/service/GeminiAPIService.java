package org.example.service;

import lombok.Builder;
import lombok.Data;
import org.example.dto.AIAnalysisRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class GeminiAPIService {
    public String generateAnalysis(MoodDataAnalysis dataAnalysis, AIAnalysisRequest.AnalysisPreferences preferences) {
        // This is a mock implementation. In a real scenario, this would call the Gemini API.
        return "{\"summary\":{\"overallTrend\":\"improving\",\"averageScore\":7.2,\"keyInsights\":[\"Insight 1\",\"Insight 2\"],\"urgencyLevel\":\"low\"},\"patterns\":{\"weeklyPattern\":{\"bestDays\":[\"Saturday\"],\"challengingDays\":[\"Wednesday\"]},\"dailyPattern\":{\"morningAverage\":6.8,\"eveningAverage\":7.4},\"triggers\":{\"positive\":[{\"factor\":\"运动\",\"frequency\":4,\"impact\":1.5}],\"negative\":[{\"factor\":\"工作压力\",\"frequency\":5,\"impact\":-1.8}]}},\"recommendations\":{\"immediate\":[{\"title\":\"Relaxation\",\"description\":\"Meditate before sleep\"}],\"shortTerm\":[],\"longTerm\":[]},\"riskAssessment\":{\"level\":\"green\",\"indicators\":[\"Good weekend recovery\"],\"suggestions\":[\"Keep it up\"]}}";
    }

    @Data
    @Builder
    public static class MoodDataAnalysis {
        private int totalEntries;
        private double averageScore;
    }
}