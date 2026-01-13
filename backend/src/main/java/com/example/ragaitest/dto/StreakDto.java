package com.example.ragaitest.dto;

import lombok.Data;

@Data
public class StreakDto {
    private Long id;
    //Eventually need to add this to session context so we don't need to pass back and forth I think
    private Long userId;
    private String name;
    private boolean completed;
    private int currentStreak;
    private int longestStreak;
}
