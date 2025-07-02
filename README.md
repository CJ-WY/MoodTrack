# MoodTrack 后端项目

## 🚀 项目简介

MoodTrack 是一个情绪管理与社区互动平台，旨在帮助用户记录每日情绪、通过 AI 分析情绪状态，并推荐用户进入具有相似情绪的社区进行交流和分享。用户可以在社区中发布帖子（支持图片），并对其他用户的帖子进行评论互动。

## 🛠️ 技术栈

本项目后端采用以下主流技术栈构建，确保了高性能、可扩展性和易维护性：

*   **Java 17**: 编程语言，提供强大的生态系统和性能。
*   **Spring Boot 3.x**: 快速开发框架，简化 Spring 应用的搭建和部署。
*   **Spring Data JPA / Hibernate**: ORM 框架，用于简化数据库操作和对象关系映射。
*   **PostgreSQL**: 关系型数据库，用于存储用户、情绪记录、帖子、评论等数据。
*   **Spring Security**: 安全框架，提供用户认证 (JWT) 和授权功能。
*   **Google Gemini API (通过 API Key)**: AI 大模型服务，用于对用户情绪进行深度分析。
*   **AWS S3**: 对象存储服务，用于存储用户上传的图片。
*   **Lombok**: 简化 Java Bean 的开发，减少样板代码。
*   **SpringDoc OpenAPI UI (Swagger)**: 自动生成和展示 RESTful API 文档。
*   **Maven**: 项目管理和构建工具。

## ✨ 功能说明

本项目提供以下核心功能：

1.  **用户管理**
    *   **用户注册**: 新用户注册账户。
    *   **用户登录**: 现有用户通过用户名和密码登录，获取 JWT。

2.  **情绪记录与 AI 分析**
    *   **提交情绪记录**: 用户提交每日心情描述、压力指数、体感状态等情绪数据。
    *   **Gemini AI 情绪分析**: 后端调用 Google Gemini API，对用户提交的情绪数据进行深度分析，生成分析文本、心理建议和匹配的情绪标签。
    *   **保存分析结果**: AI 分析结果将保存到数据库中。

3.  **社区互动**
    *   **情绪社群推荐**: 根据 AI 分析出的情绪标签，推荐用户进入具有相似情绪的帖子界面。
    *   **查看相似情绪帖子**: 用户可以在推荐的界面中查看与自己心情状况类似的用户的帖子。
    *   **发布帖子**: 用户可以在社区中发布新的帖子，支持上传图片。
    *   **评论互动**: 用户可以对帖子进行评论，支持多级嵌套回复。

## 📊 数据库表结构

本项目使用 Hibernate 注解进行建模，以下是主要数据库表及其字段：

1.  **`users` (用户信息)**
    *   `id`: 用户ID (主键)
    *   `username`: 用户名 (唯一)
    *   `email`: 邮箱 (唯一)
    *   `password`: 加密后的密码
    *   `registration_date`: 注册时间

2.  **`mood_entry` (用户每日情绪记录)**
    *   `id`: 记录ID (主键)
    *   `user_id`: 用户ID (外键，关联 `users` 表)
    *   `mood_description`: 心情描述
    *   `stress_level`: 压力指数 (1-10)
    *   `physical_state`: 体感状态
    *   `record_time`: 记录时间

3.  **`ai_analysis` (Gemini 分析结果表)**
    *   `id`: 分析结果ID (主键)
    *   `mood_entry_id`: 情绪记录ID (外键，关联 `mood_entry` 表，唯一)
    *   `analysis_text`: AI 分析结果文本
    *   `suggestion`: AI 提供的建议
    *   `matched_tags`: 匹配到的情绪标签 (多对多关联 `tag` 表)

4.  **`post` (社区发帖)**
    *   `id`: 帖子ID (主键)
    *   `author_id`: 作者ID (外键，关联 `users` 表)
    *   `title`: 标题
    *   `content`: 正文
    *   `image_url`: 图片 URL (S3 存储链接)
    *   `post_time`: 发帖时间

5.  **`comment` (评论内容)**
    *   `id`: 评论ID (主键)
    *   `post_id`: 帖子ID (外键，关联 `post` 表)
    *   `commenter_id`: 评论者ID (外键，关联 `users` 表)
    *   `content`: 评论内容
    *   `comment_time`: 评论时间
    *   `parent_comment_id`: 父评论ID (外键，关联 `comment` 表，用于嵌套评论)

6.  **`tag` (情绪群体标签)**
    *   `id`: 标签ID (主键)
    *   `name`: 标签名称 (唯一，如“焦虑群体”、“孤独型”)

7.  **`post_tag` (帖子与标签多对多映射表)**
    *   `post_id`: 帖子ID (复合主键，外键，关联 `post` 表)
    *   `tag_id`: 标签ID (复合主键，外键，关联 `tag` 表)

8.  **`ai_analysis_tag` (AI 分析结果与标签多对多映射表)**
    *   `ai_analysis_id`: AI 分析结果ID (复合主键，外键，关联 `ai_analysis` 表)
    *   `tag_id`: 标签ID (复合主键，外键，关联 `tag` 表)

## 🔗 数据库连接方式

本项目使用 PostgreSQL 数据库，并通过 Spring Data JPA 和 Hibernate 进行连接。数据库连接信息配置在 `src/main/resources/application.properties` 文件中。

**示例配置 (已更新为 Session Pooler 方式):**

```properties
# PostgreSQL Database Configuration for Supabase (using Session Pooler)
spring.datasource.url=jdbc:postgresql://aws-0-ca-central-1.pooler.supabase.com:5432/postgres
spring.datasource.username=postgres.fnqdzhgittuwsglrzcxx
spring.datasource.password=moodtrack2025
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA/Hibernate Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

**注意**: `spring.jpa.hibernate.ddl-auto=update` 会在应用启动时自动根据实体类创建或更新数据库表结构。在生产环境中，建议使用 `validate` 或 `none`，并通过 Flyway/Liquibase 等工具进行数据库迁移管理。

## 运行方法

### 环境变量配置

在运行项目之前，请确保设置了以下环境变量：

*   `AWS_ACCESS_KEY_ID`: 你的 AWS Access Key ID。
*   `AWS_SECRET_ACCESS_KEY`: 你的 AWS Secret Access Key。
*   `GEMINI_API_KEY`: 你的 Google Gemini API Key。

**在 IntelliJ IDEA 中设置 (推荐本地开发):**

1.  打开 `Run/Debug Configurations`。
2.  找到你的 Spring Boot 应用配置。
3.  在 `Environment variables` 字段中添加上述变量及其值。

**在命令行中设置 (macOS/Linux):**

```bash
export AWS_ACCESS_KEY_ID='你的AccessKeyId'
export AWS_SECRET_ACCESS_KEY='你的SecretAccessKey'
export GEMINI_API_KEY='你的GeminiApiKey'
```

### 本地运行步骤

1.  **克隆项目**: 将项目克隆到本地。
    ```bash
    git clone <项目仓库地址>
    cd MoodTrack
    ```
2.  **配置 `application.properties`**: 根据你的 Supabase 数据库和 AWS S3 配置，更新 `src/main/resources/application.properties` 文件中的相应字段。
3.  **构建项目**: 使用 Maven 构建项目。
    ```bash
    mvn clean install
    ```
4.  **运行项目**: 运行 Spring Boot 应用。
    ```bash
    mvn spring-boot:run
    ```
    或者在 IntelliJ IDEA 中直接运行 `App.java` 的 `main` 方法。

## 📄 Swagger 接口文档

项目启动后，你可以通过以下地址访问交互式 API 文档 (Swagger UI)：

`http://localhost:8080/swagger-ui/index.html`

所有 RESTful 接口都已添加中文描述，方便前端开发人员理解和测试。

## ☁️ 部署方式说明 (Render 平台)

本项目已包含 `render.yaml` 配置文件，支持一键部署到 Render 平台。

### 部署步骤 (Render)

1.  **将项目推送到 GitHub/GitLab/Bitbucket**。
2.  **登录 Render Dashboard**。
3.  点击 `New` -> `Web Service`。
4.  选择你的项目仓库。
5.  在配置过程中，Render 会自动检测 `render.yaml` 文件并应用其中的配置。
    *   **数据库**: `render.yaml` 中已定义了一个名为 `moodtrack-db` 的 PostgreSQL 数据库。Render 会自动创建并链接它。
    *   **环境变量**: 你需要在 Render Dashboard 中手动设置 `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, `GEMINI_API_KEY`, `JWT_SECRET`, `AWS_S3_BUCKETNAME`, `AWS_S3_REGION` 等环境变量。Render 会自动注入数据库连接相关的环境变量。
6.  点击 `Create Web Service`。

Render 将自动拉取代码、构建项目并部署。部署成功后，你将获得一个公开的 URL 来访问你的后端服务。

## 💡 后续功能规划

*   **用户个人主页**: 展示用户的情绪记录历史、发布的帖子和评论。
*   **情绪趋势分析**: 可视化用户情绪随时间的变化趋势。
*   **高级搜索与过滤**: 允许用户根据标签、关键词、时间范围等条件搜索帖子和评论。
*   **通知系统**: 当有新评论或推荐帖子时通知用户。
*   **用户关注/私信**: 增加用户间的社交互动功能。
*   **管理员后台**: 用于管理用户、帖子、评论和标签。
*   **更精细的 AI 分析**: 探索 Gemini 模型的更多能力，例如情绪强度、情绪转变原因分析等。
*   **多语言支持**: 支持不同语言的用户界面和内容。

---

**感谢使用 MoodTrack！**
