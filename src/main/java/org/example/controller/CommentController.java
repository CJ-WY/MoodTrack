package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.model.Comment;
import org.example.service.CommentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 评论控制器。
 * <p>
 * 负责处理与帖子评论相关的 HTTP 请求。
 * </p>
 */
@RestController
@RequestMapping("/api/comments")
@Tag(name = "评论管理接口", description = "提供评论的创建、查询等功能")
public class CommentController {

    private static final Logger logger = LoggerFactory.getLogger(CommentController.class);

    /**
     * 评论服务，负责评论的业务逻辑处理。
     */
    @Autowired
    private CommentService commentService;

    /**
     * 创建一条新的评论。
     * <p>
     * 接收评论内容、所属帖子ID和评论者ID等信息，并将其保存到数据库。
     * 支持嵌套评论，通过 `parentCommentId` 字段指定父评论。
     * </p>
     *
     * @param comment 包含评论详细信息的 {@link Comment} 对象。
     * @return 创建成功后保存到数据库的 {@link Comment} 对象。
     */
    @Operation(summary = "创建评论", description = "为帖子或现有评论创建新的评论")
    @PostMapping
    public ResponseEntity<Comment> createComment(@RequestBody Comment comment) {
        try {
            // 1. 输入参数验证
            if (comment == null) {
                logger.warn("创建评论请求失败：请求体为空。");
                return ResponseEntity.badRequest().body(null); // 400 Bad Request
            }
            if (comment.getPost() == null || comment.getPost().getId() == null) {
                logger.warn("创建评论请求失败：帖子ID为空。");
                return ResponseEntity.badRequest().body(null); // 400 Bad Request
            }
            if (comment.getCommenter() == null || comment.getCommenter().getId() == null) {
                logger.warn("创建评论请求失败：评论者ID为空。");
                // 实际应用中，评论者ID通常从 JWT 中获取，而不是从请求体中获取
                return ResponseEntity.badRequest().body(null); // 400 Bad Request
            }
            if (comment.getContent() == null || comment.getContent().trim().isEmpty()) {
                logger.warn("创建评论请求失败：评论内容为空。");
                return ResponseEntity.badRequest().body(null); // 400 Bad Request
            }

            // 2. 调用服务层创建评论
            Comment createdComment = commentService.createComment(comment);
            logger.info("评论创建成功，评论ID: {}", createdComment.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdComment); // 201 Created
        } catch (Exception e) {
            logger.error("创建评论时发生内部服务器错误: {}", e.getMessage(), e); // 记录完整的异常堆栈
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // 500 Internal Server Error
        }
    }
}