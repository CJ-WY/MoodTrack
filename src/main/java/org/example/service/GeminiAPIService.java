package org.example.service;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.AIAnalysisRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Service
@Slf4j
public class GeminiAPIService {

    @Value("#{systemEnvironment['GEMINI_API_KEY']}")
    private String apiKey;

    @Value("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-lite:generateContent")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public String generateAnalysis(MoodDataAnalysis dataAnalysis, AIAnalysisRequest.AnalysisPreferences preferences) {
        try {
            String prompt = buildAnalysisPrompt(dataAnalysis, preferences);

            String requestBody = String.format(
                    "{\"contents\": [{\"parts\": [{\"text\": \"%s\"}]}]}",
                    prompt.replace("\"", "\\\"")
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> httpEntity = new HttpEntity<>(requestBody, headers);

            String url = apiUrl + "?key=" + apiKey;

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            } else {
                log.error("Gemini API调用失败: {} - {}", response.getStatusCode(), response.getBody());
                throw new RuntimeException("Gemini API调用失败: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Gemini API调用异常", e);
            throw new RuntimeException("AI分析服务暂时不可用", e);
        }
    }

    private String buildAnalysisPrompt(MoodDataAnalysis dataAnalysis, AIAnalysisRequest.AnalysisPreferences preferences) {
        String promptTemplate = """
你是一位专业的心理健康数据分析师。请基于以下7天的情绪数据，生成一份个性化的情绪分析报告。
# 分析要求
- 语言: %s
- 分析深度: %s
- 关注领域: %s
# 用户数据摘要
总记录数: %d
平均情绪分数: %.1f
# 输出格式
请严格按照以下JSON格式输出分析结果，不要添加任何额外的解释或文本：
{
  "summary": {
    "overallTrend": "improving",
    "averageScore": 7.2,
    "keyInsights": ["洞察1", "洞察2"],
    "urgencyLevel": "low"
  },
  "patterns": {
    "weeklyPattern": {
      "bestDays": ["Saturday"],
      "challengingDays": ["Wednesday"],
      "volatilityIndex": 1.8
    },
    "dailyPattern": {
      "morningAverage": 6.8,
      "eveningAverage": 7.4,
      "peakHours": ["19:00-21:00"]
    },
    "triggers": {
      "positive": [{"factor": "运动", "frequency": 4, "impact": 1.5}],
      "negative": [{"factor": "工作压力", "frequency": 5, "impact": -1.8}]
    }
  },
  "recommendations": {
    "immediate": [{"title": "建立晚间放松routine", "description": "在睡前1小时进行冥想或轻度阅读", "priority": "high", "estimatedImpact": "medium"}],
    "shortTerm": [],
    "longTerm": []
  },
  "riskAssessment": {
    "level": "green",
    "indicators": ["情绪整体趋势向好"],
    "suggestions": ["继续保持当前的积极生活方式"]
  }
}
# 注意事项
1. 必须使用中文（或指定的语言）输出。
2. 提供的建议需要具体、可操作。
3. 避免使用任何医疗诊断性质的语言。
4. 整体语调保持积极和正面。
5. 必须确保输出是严格合法的JSON格式。
""";
        return String.format(promptTemplate, preferences.getLanguage(), preferences.getDepth(), String.join(", ", preferences.getFocusAreas()), dataAnalysis.getTotalEntries(), dataAnalysis.getAverageScore());
    }

    @Data
    @Builder
    public static class MoodDataAnalysis {
        private int totalEntries;
        private double averageScore;
    }
}