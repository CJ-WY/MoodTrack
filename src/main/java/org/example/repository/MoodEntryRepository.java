package org.example.repository;

import org.example.model.MoodEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 用户每日情绪记录的数据仓库接口。
 * <p>
 * 继承自 JpaRepository，提供了对 {@link MoodEntry} 实体进行数据库操作的基础方法。
 * </p>
 */
@Repository
public interface MoodEntryRepository extends JpaRepository<MoodEntry, Long> {
}