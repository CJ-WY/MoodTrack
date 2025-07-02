package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.model.AiAnalysis;
import org.example.model.MoodEntry;
import org.example.model.Post;
import org.example.service.AiAnalysisService;
import org.example.service.MoodEntryService;
import org.example.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 情绪记录与分析控制器
 * <p>
 * 负责处理用户提交的情绪记录，调用 AI 进行分析，并根据分析结果推荐相关的社区帖子。
 * </p>
 */
@RestController
@RequestMapping("/api/mood")
@Tag(name = "情绪记录与AI分析接口", description = "提供情绪提交、AI分析和帖子推荐功能")
public class MoodController {

    /**
     * 情绪记录服务，负责保存用户提交的情绪数据。
     */
    @Autowired
    private MoodEntryService moodEntryService;

    /**
     * AI 分析服务，负责调用 Gemini API 分析情绪。
     */
    @Autowired
    private AiAnalysisService aiAnalysisService;

    /**
     * 帖子服务，用于根据标签获取推荐的帖子。
     */
    @Autowired
    private PostService postService;

    /**
     * 提交情绪记录并获取 AI 分析结果。
     * <p>
     * 这是核心流程的入口。用户提交原始情绪数据后，此接口会：
     * 1. 保存情绪记录到数据库。
     * 2. 调用 AI 服务对该记录进行分析。
     * 3. 将包含分析结果 (分析文本、建议、匹配标签) 的 AiAnalysis 对象返回给前端。
     * 前端可以根据返回的 `matchedTags` 来决定将用户引导到哪个情绪社群。
     * </p>
     *
     * @param moodEntry 包含心情描述、压力指数等信息的情绪记录对象。
     * @return 包含详细分析结果的 AiAnalysis 对象。
     */
    @Operation(summary = "提交情绪记录并获取AI分析", description = "保存用户情绪并返回 Gemini 的分析结果，包括建议和匹配的标签")
    @PostMapping("/submit")
    public ResponseEntity<AiAnalysis> submitMoodEntry(@RequestBody MoodEntry moodEntry) {
        // 1. 保存用户提交的原始情绪记录
        MoodEntry submittedMoodEntry = moodEntryService.submitMoodEntry(moodEntry);
        // 2. 调用 AI 服务对这条记录进行分析
        AiAnalysis analysisResult = aiAnalysisService.analyzeMood(submittedMoodEntry);
        // 3. 将完整的分析结果返回给前端
        return ResponseEntity.ok(analysisResult);
    }

    /**
     * 根据 AI 分析出的标签获取推荐的帖子列表。
     * <p>
     * 用户在得到 AI 分析结果后，前端可以使用此接口，
     * 传入 AI 返回的标签，获取一个与用户当前心境相关的帖子列表，
     * 从而将用户带入一个有共鸣的社区环境中。
     * </p>
     *
     * @param tags 一个或多个由 AI 分析得出的情绪标签。
     * @return 一个包含与标签匹配的帖子的列表。
     */
    @Operation(summary = "根据标签获取推荐帖子", description = "传入一个或多个情绪标签，获取与这些标签相关的帖子列表")
    @GetMapping("/recommended-posts")
    public ResponseEntity<List<Post>> getRecommendedPosts(
            @Parameter(description = "一个或多个情绪标签，用逗号分隔", required = true, example = "职场焦虑,人际关系困扰")
            @RequestParam List<String> tags) {
        List<Post> recommendedPosts = postService.findPostsByTags(tags);
        return ResponseEntity.ok(recommendedPosts);
    }
}