package org.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.AIAnalysisRequest;
import org.example.dto.AIAnalysisResponse;
import org.example.exception.InsufficientDataException;
import org.example.model.AiAnalysis;
import org.example.model.MoodEntry;
import org.example.model.User;
import org.example.repository.AiAnalysisRepository;
import org.example.repository.MoodEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service("analysisReportService")
@Transactional
@Slf4j
public class AnalysisReportService {

    @Autowired
    private MoodEntryRepository moodEntryRepository;

    @Autowired
    private AiAnalysisRepository aiAnalysisRepository;

    @Autowired
    private GeminiAPIService geminiAPIService;

    @Autowired
    private ObjectMapper objectMapper;

    public AIAnalysisResponse generateAnalysis(AIAnalysisRequest request, User user) {
        AIAnalysisRequest processedRequest = processRequest(request);
        List<MoodEntry> moodData = getMoodData(user.getId(), processedRequest.getDateRange());
        validateMoodData(moodData);

        GeminiAPIService.MoodDataAnalysis dataAnalysis = preprocessMoodData(moodData);
        String aiResponseJson = geminiAPIService.generateAnalysis(dataAnalysis, processedRequest.getPreferences());

        try {
            AIAnalysisResponse.AnalysisResult analysisResult = objectMapper.readValue(aiResponseJson, AIAnalysisResponse.AnalysisResult.class);
            AiAnalysis savedAnalysis = saveAnalysisResult(user.getId(), processedRequest, analysisResult, dataAnalysis);
            return convertToResponse(savedAnalysis);
        } catch (IOException e) {
            log.error("Failed to parse AI response", e);
            throw new RuntimeException("Failed to parse AI response", e);
        }
    }

    private AIAnalysisRequest processRequest(AIAnalysisRequest request) {
        if (request.getDateRange() == null) {
            request.setDateRange(new AIAnalysisRequest.DateRange());
        }
        if (request.getDateRange().getEndDate() == null) {
            request.getDateRange().setEndDate(LocalDate.now());
        }
        if (request.getDateRange().getStartDate() == null) {
            request.getDateRange().setStartDate(request.getDateRange().getEndDate().minusDays(6));
        }
        return request;
    }

    private List<MoodEntry> getMoodData(Long userId, AIAnalysisRequest.DateRange dateRange) {
        return moodEntryRepository.findByUserIdAndRecordTimeBetween(userId, dateRange.getStartDate().atStartOfDay(), dateRange.getEndDate().plusDays(1).atStartOfDay());
    }

    private void validateMoodData(List<MoodEntry> moodData) {
        if (moodData.size() < 3) {
            throw new InsufficientDataException("需要至少3天的情绪记录数据才能生成分析报告", 3, moodData.size());
        }
    }

    private GeminiAPIService.MoodDataAnalysis preprocessMoodData(List<MoodEntry> moodData) {
        double averageScore = moodData.stream().mapToInt(entry -> entry.getEmotionType().ordinal()).average().orElse(0.0);
        return GeminiAPIService.MoodDataAnalysis.builder()
                .totalEntries(moodData.size())
                .averageScore(averageScore)
                .build();
    }

    private AiAnalysis saveAnalysisResult(Long userId, AIAnalysisRequest request, AIAnalysisResponse.AnalysisResult result, GeminiAPIService.MoodDataAnalysis dataAnalysis) {
        AiAnalysis analysis = AiAnalysis.builder()
                .userId(userId)
                .reportId(UUID.randomUUID().toString())
                .analysisType(org.example.model.AnalysisType.valueOf(request.getAnalysisType().toUpperCase()))
                .startDate(request.getDateRange().getStartDate())
                .endDate(request.getDateRange().getEndDate())
                .summaryData(result.getSummary())
                .patternsData(result.getPatterns())
                .recommendationsData(result.getRecommendations())
                .riskAssessmentData(result.getRiskAssessment())
                .dataPoints(dataAnalysis.getTotalEntries())
                .confidenceScore(new BigDecimal("0.85")) // Mock value
                .apiCost(new BigDecimal("0.024")) // Mock value
                .build();
        return aiAnalysisRepository.save(analysis);
    }

    private AIAnalysisResponse convertToResponse(AiAnalysis analysis) {
        AIAnalysisResponse.AnalysisResult result = AIAnalysisResponse.AnalysisResult.builder()
                .summary(analysis.getSummaryData())
                .patterns(analysis.getPatternsData())
                .recommendations(analysis.getRecommendationsData())
                .riskAssessment(analysis.getRiskAssessmentData())
                .build();

        AIAnalysisResponse.AnalysisMetadata metadata = AIAnalysisResponse.AnalysisMetadata.builder()
                .dataPoints(analysis.getDataPoints())
                .analysisConfidence(analysis.getConfidenceScore())
                .apiCost(analysis.getApiCost())
                .build();

        return AIAnalysisResponse.builder()
                .reportId(analysis.getReportId())
                .generatedAt(analysis.getCreatedAt())
                .analysisResult(result)
                .metadata(metadata)
                .build();
    }
}
