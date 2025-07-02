package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.model.Post;
import org.example.service.PostService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 社区帖子控制器。
 * <p>
 * 负责处理与社区帖子相关的 HTTP 请求，包括创建、查询帖子等。
 * </p>
 */
@RestController
@RequestMapping("/api/posts")
@Tag(name = "社区帖子接口", description = "提供帖子的创建、查询等功能")
public class PostController {

    private static final Logger logger = LoggerFactory.getLogger(PostController.class);

    /**
     * 帖子服务，负责帖子的业务逻辑处理。
     */
    @Autowired
    private PostService postService;

    /**
     * 创建一个新帖子。
     * <p>
     * 支持上传图片作为帖子的附件。图片会上传到 S3。
     * </p>
     *
     * @param post      包含帖子标题、内容等信息的 JSON 对象。
     * @param imageFile 用户上传的图片文件 (可选)。
     * @return 创建成功后保存到数据库的帖子对象。
     */
    @Operation(summary = "创建帖子", description = "创建一个新的社区帖子，可选择上传图片")
    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<Post> createPost(
            @RequestBody(description = "帖子内容", required = true,
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Post.class)))
            @RequestPart("post") Post post,
            @Parameter(description = "帖子图片文件 (可选)")
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile) {
        try {
            // 1. 输入参数验证
            if (post == null) {
                logger.warn("创建帖子请求失败：帖子内容为空。");
                return ResponseEntity.badRequest().body(null); // 400 Bad Request
            }
            if (post.getTitle() == null || post.getTitle().trim().isEmpty()) {
                logger.warn("创建帖子请求失败：帖子标题为空。");
                return ResponseEntity.badRequest().body(null); // 400 Bad Request
            }
            if (post.getAuthor() == null || post.getAuthor().getId() == null) {
                logger.warn("创建帖子请求失败：作者ID为空。");
                // 实际应用中，作者ID通常从 JWT 中获取，而不是从请求体中获取
                return ResponseEntity.badRequest().body(null); // 400 Bad Request
            }

            // 2. 调用服务层创建帖子
            Post createdPost = postService.createPost(post, imageFile);
            logger.info("帖子创建成功，帖子ID: {}", createdPost.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdPost);
        } catch (RuntimeException e) {
            // 捕获服务层抛出的运行时异常，例如文件存储失败、数据库操作失败等
            logger.error("创建帖子时发生运行时错误: {}", e.getMessage(), e); // 记录完整的异常堆栈
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // 500 Internal Server Error
        } catch (Exception e) {
            // 捕获其他未知异常
            logger.error("创建帖子时发生未知错误: {}", e.getMessage(), e); // 记录完整的异常堆栈
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // 500 Internal Server Error
        }
    }

    /**
     * 获取所有帖子。
     *
     * @return 包含所有帖子的 {@link List} 列表。
     */
    @Operation(summary = "获取所有帖子", description = "获取社区中所有已发布的帖子")
    @GetMapping
    public ResponseEntity<List<Post>> getAllPosts() {
        try {
            List<Post> posts = postService.getAllPosts();
            logger.info("成功获取 {} 篇帖子。", posts.size());
            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            // 捕获其他未知异常
            logger.error("获取所有帖子时发生内部服务器错误: {}", e.getMessage(), e); // 记录完整的异常堆栈
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // 500 Internal Server Error
        }
    }
}