package org.example.controller;

import jakarta.validation.Valid;
import org.example.dto.CreateMoodRequest;
import org.example.dto.PaginatedMoodResponse;
import org.example.model.EmotionType;
import org.example.model.MoodEntry;
import org.example.model.User;
import org.example.service.MoodEntryService;
import org.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/moods")
public class MoodController {

    @Autowired
    private MoodEntryService moodEntryService;

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<?> createMood(@Valid @RequestBody CreateMoodRequest request) {
        User currentUser = getCurrentUser();
        MoodEntry newMood = moodEntryService.createMood(request, currentUser);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "心情记录创建成功");
        response.put("data", newMood);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<?> getMoods(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String start_date,
            @RequestParam(required = false) String end_date,
            @RequestParam(required = false) String emotion_type,
            @RequestParam(defaultValue = "desc") String order) {

        User currentUser = getCurrentUser();
        limit = Math.min(limit, 100);
        Sort.Direction direction = order.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(direction, "recordTime"));

        LocalDate startDate = start_date != null ? LocalDate.parse(start_date) : null;
        LocalDate endDate = end_date != null ? LocalDate.parse(end_date) : null;
        EmotionType emotionType = emotion_type != null ? EmotionType.valueOf(emotion_type) : null;

        PaginatedMoodResponse moods = moodEntryService.getMoods(currentUser, pageable, startDate, endDate, emotionType);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", moods);

        return ResponseEntity.ok(response);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();
        return userService.findByUsername(currentPrincipalName);
    }
}
