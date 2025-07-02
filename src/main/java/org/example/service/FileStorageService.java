package org.example.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * 文件存储服务。
 * <p>
 * 负责将文件（例如帖子图片）上传到 AWS S3 存储桶。
 * </p>
 */
@Service
public class FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    /**
     * 从 application.properties 中注入 S3 存储桶的名称。
     */
    @Value("#{systemEnvironment['AWS_S3_BUCKETNAME']}")
    private String bucketName;

    /**
     * 注入 AmazonS3 客户端，用于与 S3 服务进行交互。
     */
    private final AmazonS3 s3Client;

    /**
     * 构造函数，通过依赖注入获取 AmazonS3 客户端实例。
     *
     * @param s3Client AmazonS3 客户端实例。
     */
    public FileStorageService(AmazonS3 s3Client) {
        this.s3Client = s3Client;
    }

    /**
     * 将 MultipartFile 上传到 AWS S3。
     * <p>
     * 文件名会被随机生成以避免冲突，并设置为公共可读。
     * </p>
     *
     * @param file 需要上传的 MultipartFile 对象。
     * @return 上传成功后文件的公共访问 URL。
     * @throws RuntimeException 如果文件存储过程中发生 IO 错误。
     */
    public String storeFile(MultipartFile file) {
        String originalFileName = file.getOriginalFilename();
        // 生成一个唯一的 UUID 作为文件名的一部分，防止文件名冲突
        String fileName = UUID.randomUUID().toString() + "_" + originalFileName;

        try {
            // 设置文件元数据，包括内容长度和内容类型
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            // 构建 PutObjectRequest，指定存储桶、文件名、输入流和元数据
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, fileName, file.getInputStream(), metadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead); // 设置为公共可读

            // 执行文件上传操作
            s3Client.putObject(putObjectRequest);
            logger.info("文件 {} 成功上传到 S3 存储桶 {}，文件名为 {}.", originalFileName, bucketName, fileName);

            // 返回存储对象的公共 URL
            return s3Client.getUrl(bucketName, fileName).toString();
        } catch (IOException ex) {
            logger.error("无法存储文件 {}. 错误信息: {}", fileName, ex.getMessage());
            throw new RuntimeException("无法存储文件 " + fileName + ". 请重试！", ex);
        }
    }

    /**
     * 获取 S3 上文件的公共访问 URL。
     * <p>
     * 注意：通常在调用 {@code storeFile} 后直接返回 URL，
     * 此方法可能在某些特定场景下才需要，例如根据文件名重新获取 URL。
     * </p>
     *
     * @param fileName S3 中存储的文件名。
     * @return 文件的公共访问 URL。
     */
    public String getFileUrl(String fileName) {
        return s3Client.getUrl(bucketName, fileName).toString();
    }
}