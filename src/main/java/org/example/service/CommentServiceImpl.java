package org.example.service;

import org.example.model.Comment;
import org.example.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 评论服务实现类。
 * <p>
 * 实现了 {@link CommentService} 接口，处理评论相关的业务逻辑。
 * </p>
 */
@Service
public class CommentServiceImpl implements CommentService {

    /**
     * 评论数据仓库，用于与数据库进行交互。
     */
    @Autowired
    private CommentRepository commentRepository;

    /**
     * 创建一条新的评论。
     * <p>
     * 在保存评论之前，会自动设置评论的发布时间为当前时间。
     * </p>
     *
     * @param comment 包含评论详细信息的 Comment 对象。
     * @return 创建成功后保存到数据库的 Comment 对象。
     */
    @Override
    public Comment createComment(Comment comment) {
        comment.setCommentTime(LocalDateTime.now());
        return commentRepository.save(comment);
    }
}