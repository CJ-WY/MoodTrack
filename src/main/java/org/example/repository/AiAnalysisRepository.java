package org.example.repository;

import org.example.model.AiAnalysis;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface AiAnalysisRepository extends JpaRepository<AiAnalysis, Long> {
    Optional<AiAnalysis> findByReportId(String reportId);
    Page<AiAnalysis> findByUserIdAndCreatedAtBetween(Long userId, LocalDate startDate, LocalDate endDate, Pageable pageable);
    Page<AiAnalysis> findByUserId(Long userId, Pageable pageable);
}
