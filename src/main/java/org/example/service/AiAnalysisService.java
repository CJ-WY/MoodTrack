package org.example.service;

import org.example.model.AiAnalysis;
import org.example.model.MoodEntry;

/**
 * AI 分析服务接口。
 * <p>
 * 定义了对用户情绪记录进行 AI 分析的业务逻辑操作。
 * </p>
 */
public interface AiAnalysisService {

    /**
     * 分析用户提交的情绪记录。
     * <p>
     * 该方法将调用外部 AI 服务（如 Google Gemini API）对情绪记录进行分析，
     * 并返回分析结果。
     * </p>
     *
     * @param moodEntry 情绪记录实体，包含了用户ID、心情描述、压力指数等信息。
     * @return 分析结果实体 {@link AiAnalysis}，包含了分析的详细结果。
     */
    AiAnalysis analyzeMood(MoodEntry moodEntry);
}