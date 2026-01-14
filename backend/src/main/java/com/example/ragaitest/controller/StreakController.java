package com.example.ragaitest.controller;

import com.example.ragaitest.dto.StreakDto;
import com.example.ragaitest.dto.StreakSummaryProjection;
import com.example.ragaitest.services.StreakService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/streaks")
@CrossOrigin
public class StreakController {
    private final StreakService streakService;

    @InitBinder
    void initBinder(WebDataBinder binder) {
        String[] faqBeanAllowedFields = {"id", "name", "completedToday", "currentStreak", "longestStreak"};

        binder.setAllowedFields(faqBeanAllowedFields);
    }

    @GetMapping
    @ResponseBody
    //Need to add userId here when implemented
    public ResponseEntity<List<StreakDto>> getStreaksByUserId() {
        return ResponseEntity.ok(streakService.getStreaksByUserId());
    }

    @PostMapping("/add-streak")
    public ResponseEntity<StreakDto> addStreak(@RequestBody @Valid StreakDto streakDto) {
        return new ResponseEntity<>(streakService.addStreak(streakDto), HttpStatus.OK);
    }
}
