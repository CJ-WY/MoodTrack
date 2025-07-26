package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.retry.annotation.EnableRetry;

/**
 * Spring Boot 应用启动类。
 * <p>
 * 这是整个 MoodTrack 后端服务的入口点。
 * 使用 {@code @SpringBootApplication} 注解简化了 Spring Boot 应用的配置和启动。
 * </p>
 */
@SpringBootApplication
@EnableRetry
public class App {

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    /**
     * 应用的主方法。
     * <p>
     * 这是 Java 应用程序的起点。它通过调用 {@code SpringApplication.run()} 方法来启动 Spring Boot 应用。
     * </p>
     *
     * @param args 命令行参数，可以在应用启动时传递。
     */
    public static void main(String[] args) {
        // 启动 Spring Boot 应用程序
        SpringApplication.run(App.class, args);
    }

    @Bean
    public String testBean() {
        logger.info("Test Bean created!");
        return "Hello from Test Bean";
    }
}