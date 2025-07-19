package org.example.service;

import org.example.dto.CreateMoodRequest;
import org.example.dto.PaginatedMoodResponse;
import org.example.model.EmotionType;
import org.example.model.MoodEntry;
import org.example.model.User;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface MoodEntryService {
    PaginatedMoodResponse getMoods(User user, Pageable pageable, LocalDate startDate, LocalDate endDate, EmotionType emotionType);
    MoodEntry createMood(CreateMoodRequest request, User user);
}