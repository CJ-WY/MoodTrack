package org.example.service;

import org.example.model.Comment;

/**
 * 评论服务接口。
 * <p>
 * 定义了与评论相关的业务逻辑操作。
 * </p>
 */
public interface CommentService {

    /**
     * 创建一条新的评论。
     * <p>
     * 接收评论详细信息，并将其保存到数据库。
     * </p>
     *
     * @param comment 包含评论详细信息的 {@link Comment} 对象。
     * @return 创建成功后保存到数据库的 {@link Comment} 对象。
     */
    Comment createComment(Comment comment);
}
