package com.example.ragaitest.service;

import com.example.ragaitest.dto.HeatmapItem;
import com.example.ragaitest.dto.HeatmapResponse;
import com.example.ragaitest.dto.StreakDto;
import com.example.ragaitest.dto.StreakDtoProjection;
import com.example.ragaitest.entity.StreakCompletionEntity;
import com.example.ragaitest.entity.StreakEntity;
import com.example.ragaitest.entity.UserEntity;
import com.example.ragaitest.repository.StreakCompletionRepository;
import com.example.ragaitest.repository.StreakRepository;
import com.example.ragaitest.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StreakService {
    private final StreakRepository streakRepository;
    private final StreakCompletionRepository streakCompletionRepository;
    private final UserRepository userRepository;

    public List<StreakDto> findStreakSummaries(Long userId) {
        // 1. Fetch the user's streaks
        List<StreakEntity> userStreaks = streakRepository.findByUserId(userId);
        if (userStreaks.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> streakIds = userStreaks.stream().map(StreakEntity::getId).toList();

        List<StreakCompletionEntity> validCompletions = streakCompletionRepository
                .findByStreakIdInAndCompletionDateIsNotNullOrderByReferenceDateAsc(streakIds);

        // 3. Group valid completions by streak ID and extract just the sorted dates
        Map<Long, List<LocalDate>> datesByStreak = validCompletions.stream()
                .collect(Collectors.groupingBy(
                        completion -> completion.getStreak().getId(),
                        Collectors.mapping(StreakCompletionEntity::getReferenceDate, Collectors.toList())
                ));

        LocalDate today = LocalDate.now();
        List<StreakDto> results = new ArrayList<>();

        // 4. Calculate metrics for each streak
        for (StreakEntity streak : userStreaks) {
            List<LocalDate> sortedDates = datesByStreak.getOrDefault(streak.getId(), Collections.emptyList());

            // Distinct is handled nicely since you have a unique constraint on STREAK_ID + REFERENCE_DATE,
            // but calling distinct() in Java is a safe guardrail.
            List<LocalDate> distinctSortedDates = sortedDates.stream().distinct().toList();

            boolean completedToday = distinctSortedDates.contains(today);
            long currentStreak = 0L;
            long longestStreak = 0L;

            if (!distinctSortedDates.isEmpty()) {
                int currentIslandLength = 1;
                int maxIslandLength = 1;

                // Loop through dates to find contiguous islands
                for (int i = 1; i < distinctSortedDates.size(); i++) {
                    LocalDate previousDay = distinctSortedDates.get(i - 1);
                    LocalDate currentDay = distinctSortedDates.get(i);

                    if (currentDay.minusDays(1).equals(previousDay)) {
                        // Consecutive days: grow the island
                        currentIslandLength++;
                    } else {
                        // Gap found: save the max, reset the counter
                        maxIslandLength = Math.max(maxIslandLength, currentIslandLength);
                        currentIslandLength = 1;
                    }
                }

                // Catch the final island size after the loop
                longestStreak = Math.max(maxIslandLength, currentIslandLength);

                // Determine active current streak
                LocalDate lastCompletion = distinctSortedDates.getLast();
                if (lastCompletion.equals(today) || lastCompletion.equals(today.minusDays(1))) {
                    // If the latest completion is today or yesterday, the streak is alive
                    currentStreak = currentIslandLength;
                }
            }

            results.add(new StreakDto(
                    streak.getId(),
                    streak.getName(),
                    completedToday,
                    currentStreak,
                    longestStreak
            ));
        }

        return results;
    }

    @Transactional
    public StreakDto addStreak(StreakDto streakDto, String username) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        StreakEntity streakEntity = new StreakEntity();
        streakEntity.setUser(user);
        streakEntity.setName(streakDto.getName());
        streakEntity = streakRepository.save(streakEntity);
        streakDto.setId(streakEntity.getId());
        return streakDto;
    }

    @Transactional
    public void deleteStreak(Long id, String username) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        StreakEntity streak = streakRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Streak not found"));
        
        if (!streak.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Not authorized to delete this streak");
        }
        
        streakRepository.deleteById(id);
    }

    @Transactional
    public void updateStreak(StreakDto streakDto, String username) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        var streakEntity = streakRepository.findById(streakDto.getId());
        streakEntity.ifPresent(
                streak -> {
                    if (!streak.getUser().getId().equals(user.getId())) {
                        throw new RuntimeException("Not authorized to update this streak");
                    }
                    
                    streak.setName(streakDto.getName());
                    var completionEntity = streakCompletionRepository
                            .findByStreakIdAndReferenceDate(streak.getId(), LocalDate.now())
                            .orElseGet(() -> {
                                var newEntity = new StreakCompletionEntity();
                                newEntity.setStreak(streak);
                                newEntity.setReferenceDate(LocalDate.now());
                                return newEntity;
                            });

                    if (streakDto.getCompletedToday() != null && streakDto.getCompletedToday()) {
                        if (completionEntity.getCompletionDate() == null) {
                            completionEntity.setCompletionDate(Instant.now());
                        }
                    } else {
                        completionEntity.setCompletionDate(null);
                    }

                    streakCompletionRepository.save(completionEntity);
                }
        );
    }

    public HeatmapResponse getHeatmap(Long streakId, int days) {
        // 1. Calculate boundaries
        LocalDate today = LocalDate.now();
        // Subtracting (days - 1) ensures today is included in the total count
        LocalDate startDate = today.minusDays(days - 1);

        // 2. Fetch completed entities
        List<StreakCompletionEntity> completedEntities = streakCompletionRepository
                .findByStreakIdAndReferenceDateBetweenAndCompletionDateIsNotNull(streakId, startDate, today);

        // 3. Extract dates into a fast HashSet for O(1) lookups
        Set<LocalDate> completedDates = completedEntities.stream()
                .map(StreakCompletionEntity::getReferenceDate)
                .collect(Collectors.toSet());

        // 4. Build the grid skeleton
        List<HeatmapItem> grid = new ArrayList<>();
        int completedCount = 0;

        LocalDate tomorrow = today.plusDays(1);
        // Loop day-by-day from the start date up to (and including) today
        for (LocalDate date = startDate; date.isBefore(tomorrow); date = date.plusDays(1)) {

            // Check if this specific day exists in our database results
            boolean isCompleted = completedDates.contains(date);

            grid.add(new HeatmapItem(date, isCompleted));

            if (isCompleted) {
                completedCount++;
            }
        }

        // 5. Calculate stats safely
        int totalDays = grid.size(); // This will perfectly match your 'days' parameter

        // Cast to double to prevent Java from doing integer division (which drops decimals)
        double percentage = totalDays == 0 ? 0.0 :
                Math.round(((double) completedCount / totalDays) * 1000.0) / 10.0;

        // 6. Return the formatted response
        return new HeatmapResponse(grid, percentage, totalDays, completedCount);
    }
}
