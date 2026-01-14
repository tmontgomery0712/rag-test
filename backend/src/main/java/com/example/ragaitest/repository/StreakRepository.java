package com.example.ragaitest.repository;

import com.example.ragaitest.dto.StreakDto;
import com.example.ragaitest.dto.StreakSummaryProjection;
import com.example.ragaitest.entity.StreakEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StreakRepository extends JpaRepository<StreakEntity, Long> {

    @Query(value = """
        WITH ranked_completions AS (
            SELECT
                streak_id,
                completion_date,
                -- Oracle/H2 Math: Date - Integer = Date
                (completion_date - ROW_NUMBER() OVER (PARTITION BY streak_id ORDER BY completion_date)) as island_id
            FROM streak_completions
            WHERE completed = 1
        ),
        streak_groups AS (
            SELECT
                streak_id,
                COUNT(*) as streak_length,
                MAX(completion_date) as last_date
            FROM ranked_completions
            GROUP BY streak_id, island_id
        )
        SELECT
            s.id AS id,
            s.name AS name,
            -- CHECK FOR TODAY
            CASE WHEN EXISTS (
                SELECT 1 FROM streak_completions sc
                WHERE sc.streak_id = s.id
                  AND sc.completion_date = CURRENT_DATE
                  AND sc.completed = 1
            ) THEN 1 ELSE 0 END AS completedToday,
            -- CURRENT STREAK
            COALESCE(MAX(CASE
                WHEN sg.last_date >= (CURRENT_DATE - 1)
                THEN sg.streak_length
                ELSE 0
            END), 0) AS currentStreak,
            -- LONGEST STREAK
            COALESCE(MAX(sg.streak_length), 0) AS longestStreak
        FROM streaks s
        LEFT JOIN streak_groups sg ON s.id = sg.streak_id
        WHERE s.user_id = :userId
        GROUP BY s.id, s.name
        """, nativeQuery = true)
    List<StreakDto> findStreakSummaries(@Param("userId") Long userId);
}
