package com.example.ragaitest.dto;

public interface StreakDtoProjection {
    Long getId();
    String getName();
    Boolean getCompletedToday();
    Long getCurrentStreak();
    Long getLongestStreak();
}
