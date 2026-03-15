package com.example.ragaitest.dto;

import java.time.LocalDate;

public record HeatmapItem(
        LocalDate date,
        boolean completed
) {}
