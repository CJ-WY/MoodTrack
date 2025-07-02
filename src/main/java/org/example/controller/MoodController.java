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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 情绪记录与分析控制器。
 * <p>
 * 负责处理用户提交的情绪记录，调用 AI 进行分析，并根据分析结果推荐相关的社区帖子。
 * </p>
 */
@RestController
@RequestMapping("/api/mood")
@Tag(name = "情绪记录与AI分析接口", description = "提供情绪提交、AI分析和帖子推荐功能")
public class MoodController {

    private static final Logger logger = LoggerFactory.getLogger(MoodController.class);

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
     * @return 包含详细分析结果的 {@link AiAnalysis} 对象。
     */
    @Operation(summary = "提交情绪记录并获取AI分析", description = "保存用户情绪并返回 Gemini 的分析结果，包括建议和匹配的标签")
    @PostMapping("/submit")
    public ResponseEntity<AiAnalysis> submitMoodEntry(@RequestBody MoodEntry moodEntry) {
        try {
            // 1. 输入参数验证
            if (moodEntry == null) {
                logger.warn("提交情绪记录请求失败：请求体为空。");
                return ResponseEntity.badRequest().body(null); // 400 Bad Request
            }
            if (moodEntry.getMoodDescription() == null || moodEntry.getMoodDescription().trim().isEmpty()) {
                logger.warn("提交情绪记录请求失败：心情描述为空。");
                return ResponseEntity.badRequest().body(null); // 400 Bad Request
            }
            if (moodEntry.getStressLevel() == null || moodEntry.getStressLevel() < 1 || moodEntry.getStressLevel() > 10) {
                logger.warn("提交情绪记录请求失败：压力指数不在有效范围 (1-10)。");
                return ResponseEntity.badRequest().body(null); // 400 Bad Request
            }
            // 假设 user 已经通过 JWT 认证获取，这里不再进行详细的用户查找
            if (moodEntry.getUser() == null || moodEntry.getUser().getId() == null) {
                logger.warn("提交情绪记录请求失败：用户ID为空。");
                return ResponseEntity.badRequest().body(null); // 400 Bad Request
            }

            // 2. 保存用户提交的原始情绪记录
            MoodEntry submittedMoodEntry = moodEntryService.submitMoodEntry(moodEntry);
            logger.info("情绪记录提交成功，记录ID: {}", submittedMoodEntry.getId());

            // 3. 调用 AI 服务对这条记录进行分析
            AiAnalysis analysisResult = aiAnalysisService.analyzeMood(submittedMoodEntry);
            logger.info("AI 分析完成，分析结果ID: {}", analysisResult.getId());

            // 4. 将完整的分析结果返回给前端
            return ResponseEntity.status(HttpStatus.CREATED).body(analysisResult);
        } catch (RuntimeException e) {
            // 捕获服务层抛出的运行时异常，例如 AI 服务调用失败、数据库操作失败等
            logger.error("提交情绪记录或AI分析时发生运行时错误: {}", e.getMessage(), e); // 记录完整的异常堆栈
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // 500 Internal Server Error
        } catch (Exception e) {
            // 捕获其他未知异常
            logger.error("提交情绪记录或AI分析时发生未知错误: {}", e.getMessage(), e); // 记录完整的异常堆栈
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // 500 Internal Server Error
        }
    }

    /**
     * 根据 AI 分析出的标签获取推荐的帖子列表。
     * <p>
     * 用户在得到 AI 分析结果后，前端可以使用此接口，
     * 传入 AI 返回的标签，获取一个与用户当前心境相关的帖子列表，
     * 从而将用户带入一个有共鸣的社区环境中。
     * </p>
     *
     * @param tags 一个或多个由 AI 分析得出的情绪标签，用逗号分隔。
     * @return 一个包含与标签匹配的帖子的 {@link List} 列表。
     */
    @Operation(summary = "根据标签获取推荐帖子", description = "传入一个或多个情绪标签，获取与这些标签相关的帖子列表")
    @GetMapping("/recommended-posts")
    public ResponseEntity<List<Post>> getRecommendedPosts(
            @Parameter(description = "一个或多个情绪标签，用逗号分隔", required = true, example = "职场焦虑,人际关系困扰")
            @RequestParam List<String> tags) {
        try {
            // 1. 输入参数验证
            if (tags == null || tags.isEmpty()) {
                logger.warn("获取推荐帖子请求失败：标签列表为空。");
                return ResponseEntity.badRequest().body(null); // 400 Bad Request
            }

            // 2. 调用服务层查找帖子
            List<Post> recommendedPosts = postService.findPostsByTags(tags);
            logger.info("根据标签 {} 找到 {} 篇推荐帖子。", tags, recommendedPosts.size());
            return ResponseEntity.ok(recommendedPosts);
        } catch (Exception e) {
            // 捕获其他未知异常
            logger.error("获取推荐帖子时发生内部服务器错误: {}", e.getMessage(), e); // 记录完整的异常堆栈
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // 500 Internal Server Error
        }
    }
}