package com.example.ragaitest.dto;

public record StreakSummaryProjection(
        Long getStreakId,
        String getName,
        Boolean getCompletedToday,
        Long getCurrentStreak,
        Long getLongestStreak
) {}
