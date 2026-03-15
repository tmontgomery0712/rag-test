package com.example.ragaitest.repository;

import com.example.ragaitest.entity.StreakCompletionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StreakCompletionRepository extends JpaRepository<StreakCompletionEntity, Long> {

    Optional<StreakCompletionEntity> findByStreakIdAndReferenceDate(Long streakId, LocalDate referenceDate);

    List<StreakCompletionEntity> findByStreakIdInAndCompletionDateIsNotNullOrderByReferenceDateAsc(List<Long> streakIds);

    List<StreakCompletionEntity> findByStreakIdAndReferenceDateBetweenAndCompletionDateIsNotNull(
            Long streakId,
            LocalDate startDate,
            LocalDate endDate
    );
}
