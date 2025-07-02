package org.example.repository;

import org.example.model.AiAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * AI 分析结果的数据仓库接口。
 * <p>
 * 继承自 JpaRepository，提供了对 {@link AiAnalysis} 实体进行数据库操作的基础方法。
 * </p>
 */
@Repository
public interface AiAnalysisRepository extends JpaRepository<AiAnalysis, Long> {
}