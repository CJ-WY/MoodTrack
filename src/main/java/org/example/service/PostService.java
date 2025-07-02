package org.example.service;

import org.example.model.Post;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 帖子服务接口
 * <p>
 * 定义了与社区帖子相关的业务逻辑操作。
 * </p>
 */
public interface PostService {

    /**
     * 创建一个新帖子。
     *
     * @param post      包含标题、内容等信息的帖子对象。
     * @param imageFile 用户上传的图片文件 (可选)。
     * @return 创建并保存到数据库后的帖子对象。
     */
    Post createPost(Post post, MultipartFile imageFile);

    /**
     * 获取所有帖子。
     * <p>
     * 在实际生产环境中，这里应该使用分页 (Paging) 来处理大量数据。
     * 为简化起见，目前返回所有帖子。
     * </p>
     *
     * @return 包含所有帖子的列表。
     */
    List<Post> getAllPosts();

    /**
     * 根据一个或多个标签名称，查找所有相关的帖子。
     *
     * @param tagNames 标签名称的列表。
     * @return 匹配到的帖子列表。
     */
    List<Post> findPostsByTags(List<String> tagNames);

}