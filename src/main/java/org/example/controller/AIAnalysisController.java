package org.example.controller;

import jakarta.validation.Valid;
import org.example.dto.AIAnalysisRequest;
import org.example.dto.AIAnalysisResponse;
import org.example.model.User;
import org.example.service.AnalysisReportService;
import org.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai-analysis")
public class AIAnalysisController {

    @Autowired
    private AnalysisReportService analysisReportService;

    @Autowired
    private UserService userService;

    @PostMapping("/generate")
    public ResponseEntity<?> generateAnalysis(@Valid @RequestBody AIAnalysisRequest request) {
        User currentUser = getCurrentUser();
        AIAnalysisResponse response = analysisReportService.generateAnalysis(request, currentUser);
        Map<String, Object> successResponse = new HashMap<>();
        successResponse.put("success", true);
        successResponse.put("message", "AI分析报告生成成功");
        successResponse.put("data", response);
        return ResponseEntity.ok(successResponse);
    }

    // History and Get by ID endpoints would be implemented here

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();
        return userService.findByUsername(currentPrincipalName);
    }
}