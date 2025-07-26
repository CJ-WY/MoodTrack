package org.example.service;

import org.example.dto.AIAnalysisRequest;
import org.example.dto.AIAnalysisResponse;
import org.example.model.AiAnalysis;
import org.example.model.MoodEntry;
import org.example.model.User;

public interface AiAnalysisService {
    AiAnalysis analyzeMood(MoodEntry moodEntry);
    AIAnalysisResponse generateAnalysis(AIAnalysisRequest request, User user);
}
