package com.example.ragaitest.dto;

import lombok.Data;

@Data
public class StreakDto {
    private Long id;
    private String name;
    private boolean completed;
    private int currentStreak;
    private int longestStreak;
}
