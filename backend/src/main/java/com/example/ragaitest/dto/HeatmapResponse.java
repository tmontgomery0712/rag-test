package com.example.ragaitest.dto;

import java.util.List;

public record HeatmapResponse(
        List<HeatmapItem> grid,
        double completionPercentage,
        int totalDays,
        int completedDays
) {}
