package org.example.service;

import org.example.model.AiAnalysis;
import org.example.model.MoodEntry;

/**
 * AI 分析服务接口
 */
public interface AiAnalysisService {

    /**
     * 分析情绪
     *
     * @param moodEntry 情绪记录
     * @return 分析结果
     */
    AiAnalysis analyzeMood(MoodEntry moodEntry);
}
