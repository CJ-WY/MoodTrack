package org.example.service;

import org.example.model.Post;
import org.example.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 帖子服务实现类
 * <p>
 * 实现了帖子相关的业务逻辑，包括创建帖子、获取所有帖子以及根据标签查找帖子。
 * </p>
 */
@Service
public class PostServiceImpl implements PostService {

    /**
     * 帖子数据仓库，用于与数据库进行交互。
     */
    @Autowired
    private PostRepository postRepository;

    /**
     * 文件存储服务，用于处理图片上传到 S3。
     */
    @Autowired
    private FileStorageService fileStorageService;

    /**
     * 创建一个新帖子。
     * <p>
     * 如果提供了图片文件，则先将图片上传到 S3 并获取其 URL，然后将帖子信息保存到数据库。
     * </p>
     *
     * @param post      包含标题、内容等信息的帖子对象。
     * @param imageFile 用户上传的图片文件 (可选)。
     * @return 创建并保存到数据库后的帖子对象。
     */
    @Override
    public Post createPost(Post post, MultipartFile imageFile) {
        // 如果存在图片文件且不为空，则上传到 S3 并设置图片 URL
        if (imageFile != null && !imageFile.isEmpty()) {
            String imageUrl = fileStorageService.storeFile(imageFile);
            post.setImageUrl(imageUrl);
        }
        // 设置帖子的发布时间为当前时间
        post.setPostTime(LocalDateTime.now());
        // 保存帖子到数据库
        return postRepository.save(post);
    }

    /**
     * 获取所有帖子。
     *
     * @return 包含所有帖子的列表。
     */
    @Override
    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    /**
     * 根据一个或多个标签名称，查找所有相关的帖子。
     * <p>
     * 调用 PostRepository 中定义的自定义查询方法。
     * </p>
     *
     * @param tagNames 标签名称的列表。
     * @return 匹配到的帖子列表。
     */
    @Override
    public List<Post> findPostsByTags(List<String> tagNames) {
        return postRepository.findPostsByTagNames(tagNames);
    }
}