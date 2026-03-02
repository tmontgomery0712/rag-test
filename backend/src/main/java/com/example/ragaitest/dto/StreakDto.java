package com.example.ragaitest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StreakDto {
    private Long id;
    private String name;
    private Boolean completedToday;
    private Long currentStreak;
    private Long longestStreak;
}
