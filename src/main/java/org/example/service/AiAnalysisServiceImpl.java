package org.example.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import org.example.model.AiAnalysis;
import org.example.model.MoodEntry;
import org.example.model.Tag;
import org.example.repository.AiAnalysisRepository;
import org.example.repository.TagRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AI 分析服务实现类。
 * <p>
 * 负责通过直接 HTTP 请求调用 Google Gemini API (使用 API Key) 对用户的情绪记录进行分析。
 * </p>
 */
@Service
public class AiAnalysisServiceImpl implements AiAnalysisService {

    // 日志记录器，用于在控制台输出程序运行信息和错误
    private static final Logger logger = LoggerFactory.getLogger(AiAnalysisServiceImpl.class);

    // Gemini API 的基础 URL。
    // 使用 gemini-pro 模型，因为它通常在免费层级可用且性能良好。
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent";

    // 从环境变量中读取 Gemini API Key。
    // 这是调用 Gemini API 进行身份验证的关键。
    @Value("#{systemEnvironment['GEMINI_API_KEY']}")
    private String geminiApiKey;

    // 注入 AI 分析结果的数据仓库，用于数据库操作。
    @Autowired
    private AiAnalysisRepository aiAnalysisRepository;

    // 注入标签的数据仓库，用于查找或创建标签。
    @Autowired
    private TagRepository tagRepository;

    // Google Gson 库实例，用于 JSON 字符串的序列化和反序列化。
    private final Gson gson = new Gson();

    /**
     * 分析用户提交的情绪记录。
     * <p>
     * 本方法是项目的核心功能之一。它会执行以下操作：
     * 1. 验证 GEMINI_API_KEY 是否已设置。
     * 2. 构建一个详细的 Prompt (提示)，指导 Gemini API 如何分析数据。
     * 3. 使用 Java 11+ 的 HttpClient 发送 HTTP POST 请求到 Gemini API。
     * 4. 检查 API 响应状态码，处理可能的错误。
     * 5. 解析 API 返回的 JSON 格式的分析结果。
     * 6. 将分析结果（包括分析文本、建议、匹配的标签）保存到数据库中。
     * 7. 处理并保存与帖子关联的标签，确保标签的唯一性。
     * </p>
     *
     * @param moodEntry 需要被分析的情绪记录实体，包含了用户ID、心情描述、压力指数等信息。
     * @return 保存到数据库后的 AiAnalysis 实体，包含了分析的详细结果。
     * @throws RuntimeException 如果 GEMINI_API_KEY 未设置，或调用 AI 服务失败，或解析结果失败。
     */
    @Override
    @Transactional // 声明为事务方法，确保数据库操作的原子性
    public AiAnalysis analyzeMood(MoodEntry moodEntry) {
        logger.info("开始分析情绪记录 ID: {}", moodEntry.getId());

        // 检查 Gemini API Key 是否已设置
        if (geminiApiKey == null || geminiApiKey.isEmpty()) {
            logger.error("GEMINI_API_KEY 未设置。无法调用 Gemini API。请确保在运行环境（如IDEA环境变量、系统环境变量或Render配置）中正确设置了 GEMINI_API_KEY。");
            throw new RuntimeException("GEMINI_API_KEY 未设置，请检查环境变量。");
        }

        try {
            // 1. 构建发送给 Gemini API 的 Prompt
            String promptText = buildPrompt(moodEntry);
            logger.debug("构建的 Prompt: {}", promptText);

            // 2. 构建请求体 JSON
            // 对 Prompt 中的双引号进行转义，以确保 JSON 格式正确
            String requestBody = String.format(
                    "{\"contents\": [{\"parts\": [{\"text\": \"%s\"}]}], \"generationConfig\": {\"temperature\": 0.7}}",
                    promptText.replace("\"", "\\\"")
            );

            // 3. 初始化 HttpClient
            HttpClient client = HttpClient.newHttpClient();

            // 4. 构建 HttpRequest
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GEMINI_API_URL + "?key=" + geminiApiKey)) // 将 API Key 作为查询参数
                    .header("Content-Type", "application/json") // 设置请求头为 JSON 格式
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody)) // 设置请求方法为 POST，并附带请求体
                    .build();

            // 5. 发送请求并获取响应
            logger.info("正在调用 Gemini API...");
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            logger.info("成功接收到 Gemini API 的响应。状态码: {}", response.statusCode());

            // 6. 检查响应状态码
            if (response.statusCode() != 200) {
                String errorMessage = String.format("Gemini API 调用失败。状态码: %d，响应体: %s", response.statusCode(), response.body());
                logger.error(errorMessage);
                // 根据不同的状态码提供更具体的错误信息
                if (response.statusCode() == 400) {
                    throw new RuntimeException("Gemini API 请求无效，请检查 Prompt 或请求格式。" + errorMessage);
                } else if (response.statusCode() == 401) {
                    throw new RuntimeException("Gemini API Key 无效或未授权。" + errorMessage);
                } else if (response.statusCode() == 429) {
                    throw new RuntimeException("Gemini API 请求频率过高，请稍后重试。" + errorMessage);
                } else if (response.statusCode() == 500) {
                    throw new RuntimeException("Gemini API 服务器内部错误，请联系管理员。" + errorMessage);
                } else {
                    throw new RuntimeException("Gemini API 调用失败，未知错误。" + errorMessage);
                }
            }

            // 7. 从响应中提取纯文本内容
            String responseBody = response.body();
            logger.debug("Gemini API 返回的原始 JSON 文本: {}", responseBody);

            // 8. 解析 JSON 并创建 AiAnalysis实体
            AiAnalysis aiAnalysis = parseAnalysis(responseBody, moodEntry);

            // 9. 保存分析结果到数据库
            AiAnalysis savedAnalysis = aiAnalysisRepository.save(aiAnalysis);
            logger.info("成功保存 AI 分析结果, ID: {}", savedAnalysis.getId());

            return savedAnalysis;

        } catch (IOException e) {
            logger.error("调用 Gemini API 时发生 IO 异常: {}", e.getMessage(), e);
            throw new RuntimeException("调用 AI 服务失败，网络或 IO 错误。", e);
        } catch (InterruptedException e) {
            logger.error("调用 Gemini API 时线程中断: {}", e.getMessage(), e);
            Thread.currentThread().interrupt(); // 重新设置中断状态
            throw new RuntimeException("调用 AI 服务失败，操作被中断。", e);
        } catch (JsonSyntaxException e) {
            logger.error("解析 Gemini API 返回的 JSON 时发生错误: {}", e.getMessage(), e);
            throw new RuntimeException("解析 AI 分析结果失败，返回的 JSON 格式不正确。", e);
        } catch (Exception e) {
            logger.error("分析情绪时发生未知错误: {}", e.getMessage(), e);
            throw new RuntimeException("分析情绪时发生未知错误。", e);
        }
    }

    /**
     * 构建用于调用 Gemini API 的详细 Prompt。
     * <p>
     * Prompt 的设计至关重要，它直接决定了 AI 分析的质量和结果的格式。
     * 这个 Prompt 指示 AI 扮演一个专业的心理顾问，并要求它返回一个特定结构的 JSON 对象。
     * </p>
     *
     * @param moodEntry 用户的情绪记录。
     * @return 构建好的字符串格式的 Prompt。
     */
    private String buildPrompt(MoodEntry moodEntry) {
        return String.format(
                "请你扮演一个专业的心理健康顾问。根据以下用户提交的情绪记录，进行深入分析。" +
                        "用户的记录如下：\n" +
                        "心情描述: \"%s\"\n" +
                        "压力指数 (1-10): %d\n" +
                        "体感状态: \"%s\"\n\n" +
                        "请严格按照以下 JSON 格式返回你的分析结果，不要添加任何额外的解释或说明文字：\n" +
                        "{\n" +
                        "  \"analysisText\": \"(这里是对用户当前情绪状态的详细分析，请专业、共情地描述)\",\n" +
                        "  \"suggestion\": \"(这里提供2-3条具体的、可操作的心理健康建议，帮助用户改善状态)\",\n" +
                        "  \"matchedTags\": [\"(这里根据分析结果，生成3-5个最相关的用户群体标签，例如：学业压力, 职场焦虑, 情绪低落, 人际关系困扰, 自我怀疑, 轻度抑郁倾向, 积极心态, 成长烦恼等)\"]\n" +
                        "}",
                moodEntry.getMoodDescription(),
                moodEntry.getStressLevel(),
                moodEntry.getPhysicalState()
        );
    }

    /**
     * 解析从 Gemini API 返回的 JSON 字符串。
     * <p>
     * 从 Gemini API 的原始响应中提取分析文本、建议和匹配的标签，并填充到 AiAnalysis 实体中。
     * </p>
     *
     * @param json      API 返回的包含分析结果的原始 JSON 字符串。
     * @param moodEntry 关联的情绪记录实体。
     * @return 一个填充了分析数据的 AiAnalysis 实体。
     * @throws JsonSyntaxException 如果 JSON 格式不正确或缺少关键字段。
     */
    private AiAnalysis parseAnalysis(String json, MoodEntry moodEntry) throws JsonSyntaxException {
        // 移除 Gemini 可能返回的多余代码标记，例如 "```json" 和 "```"
        String cleanedJson = json.replace("```json", "").replace("```", "").trim();

        // 使用 Gson 将 JSON 字符串解析为 JsonObject
        JsonObject jsonObject = gson.fromJson(cleanedJson, JsonObject.class);

        // 从 JsonObject 中提取各个字段的值
        // 注意：Gemini API 的响应结构是嵌套的，需要逐层解析
        // 示例响应结构: {"candidates": [{"content": {"parts": [{"text": "YOUR_JSON_OUTPUT"}]}}]}
        String analysisText = null;
        String suggestion = null;
        List<String> tagNames = new ArrayList<>();

        try {
            // 尝试从 Gemini 响应中提取实际的 JSON 字符串
            // 这里的路径是根据 Gemini API 的典型响应结构来确定的
            String geminiOutputText = jsonObject.getAsJsonArray("candidates")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("content")
                    .getAsJsonArray("parts")
                    .get(0).getAsJsonObject()
                    .getAsJsonPrimitive("text").getAsString();

            // 再次解析 Gemini 输出的 JSON 字符串，因为 Gemini 可能会将整个 JSON 结构作为文本返回
            JsonObject innerJsonObject = gson.fromJson(geminiOutputText, JsonObject.class);

            analysisText = innerJsonObject.get("analysisText").getAsString();
            suggestion = innerJsonObject.get("suggestion").getAsString();
            tagNames = gson.fromJson(innerJsonObject.get("matchedTags"), ArrayList.class);

        } catch (Exception e) {
            logger.warn("解析 Gemini API 响应的嵌套 JSON 结构时发生错误，尝试直接解析顶层 JSON: {}", e.getMessage());
            // 如果解析嵌套结构失败，尝试直接解析顶层 JSON，以防 Gemini 直接返回了我们期望的 JSON
            try {
                analysisText = jsonObject.get("analysisText").getAsString();
                suggestion = jsonObject.get("suggestion").getAsString();
                tagNames = gson.fromJson(jsonObject.get("matchedTags"), ArrayList.class);
            } catch (Exception ex) {
                logger.error("尝试直接解析顶层 JSON 也失败: {}", ex.getMessage(), ex);
                throw new JsonSyntaxException("无法从 Gemini API 响应中解析出预期的 JSON 结构。", ex);
            }
        }


        // 创建并填充 AiAnalysis 对象
        AiAnalysis aiAnalysis = new AiAnalysis();
        aiAnalysis.setMoodEntry(moodEntry);
        aiAnalysis.setAnalysisText(analysisText);
        aiAnalysis.setSuggestion(suggestion);

        // 处理标签：查找现有标签或创建新标签
        // 确保标签名称被 trim() 以去除前后空格
        List<Tag> tags = tagNames.stream()
                .map(tagName -> tagRepository.findByName(tagName.trim())
                        .orElseGet(() -> {
                            Tag newTag = new Tag();
                            newTag.setName(tagName.trim());
                            return tagRepository.save(newTag);
                        }))
                .collect(Collectors.toList());

        // 将处理好的标签列表关联到分析结果上
        aiAnalysis.setMatchedTags(tags);

        return aiAnalysis;
    }
}
