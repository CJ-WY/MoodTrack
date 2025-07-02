package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.model.Comment;
import org.example.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 评论控制器
 * <p>
 * 负责处理与帖子评论相关的 HTTP 请求。
 * </p>
 */
@RestController
@RequestMapping("/api/comments")
@Tag(name = "评论管理接口", description = "提供评论的创建、查询等功能")
public class CommentController {

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
     * @param comment 包含评论详细信息的 Comment 对象。
     * @return 创建成功后保存到数据库的 Comment 对象。
     */
    @Operation(summary = "创建评论", description = "为帖子或现有评论创建新的评论")
    @PostMapping
    public ResponseEntity<Comment> createComment(@RequestBody Comment comment) {
        Comment createdComment = commentService.createComment(comment);
        return ResponseEntity.ok(createdComment);
    }
}