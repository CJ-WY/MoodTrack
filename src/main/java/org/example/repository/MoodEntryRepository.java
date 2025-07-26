package org.example.repository;

import org.example.model.MoodEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MoodEntryRepository extends JpaRepository<MoodEntry, Long>, JpaSpecificationExecutor<MoodEntry> {
    List<MoodEntry> findByUserIdAndRecordTimeBetween(Long userId, LocalDateTime start, LocalDateTime end);
}
