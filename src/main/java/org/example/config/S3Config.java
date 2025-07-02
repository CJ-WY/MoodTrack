package org.example.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AWS S3 配置类
 * <p>
 * 负责配置和创建 Amazon S3 客户端的 Spring Bean。
 * 这个客户端用于后续的文件上传服务，例如用户上传帖子图片。
 * </p>
 */
@Configuration
public class S3Config {

    /**
     * 从系统环境变量中注入 AWS Access Key ID。
     * 这样做比硬编码在代码中更安全。
     */
    @Value("#{systemEnvironment['AWS_ACCESS_KEY_ID']}")
    private String accessKeyId;

    /**
     * 从系统环境变量中注入 AWS Secret Access Key。
     */
    @Value("#{systemEnvironment['AWS_SECRET_ACCESS_KEY']}")
    private String secretAccessKey;

    /**
     * 从 application.properties 文件中注入 AWS S3 的区域。
     */
    @Value("#{systemEnvironment['AWS_S3_REGION']}")
    private String region;

    /**
     * 创建并配置一个 AmazonS3 客户端实例，并将其注册为 Spring Bean。
     * <p>
     * 当其他组件 (如 FileStorageService) 需要使用 S3 时，
     * Spring 会自动注入这个 Bean。
     * </p>
     *
     * @return 配置好的 AmazonS3 客户端实例。
     */
    @Bean
    public AmazonS3 s3Client() {
        // 1. 使用注入的 accessKeyId 和 secretAccessKey 创建 AWS 凭证对象
        AWSCredentials credentials = new BasicAWSCredentials(
                accessKeyId,
                secretAccessKey
        );
        // 2. 使用凭证和区域信息构建 S3 客户端
        return AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials)) // 设置凭证
                .withRegion(Regions.fromName(region)) // 设置 S3 存储桶所在的区域
                .build();
    }
}
