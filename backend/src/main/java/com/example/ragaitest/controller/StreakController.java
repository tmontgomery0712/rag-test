package com.example.ragaitest.controller;

import com.example.ragaitest.dto.StreakDto;
import com.example.ragaitest.dto.StreakDtoProjection;
import com.example.ragaitest.security.Principal;
import com.example.ragaitest.service.StreakService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/streaks")
public class StreakController {
    private final StreakService streakService;

    @InitBinder
    void initBinder(WebDataBinder binder) {
        String[] faqBeanAllowedFields = {"id", "name", "completedToday", "currentStreak", "longestStreak"};

        binder.setAllowedFields(faqBeanAllowedFields);
    }

    @GetMapping
    @ResponseBody
    public ResponseEntity<List<StreakDto>> getStreaksByUserId(@AuthenticationPrincipal Principal principal) {
        return ResponseEntity.ok(streakService.findStreakSummaries(principal.getId()));
    }

    @PostMapping("/add-streak")
    public ResponseEntity<StreakDto> addStreak(@RequestBody @Valid StreakDto streakDto, @AuthenticationPrincipal Principal principal) {
        return new ResponseEntity<>(streakService.addStreak(streakDto, principal.getUsername()), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStreak(@PathVariable Long id, @AuthenticationPrincipal Principal principal) {
        streakService.deleteStreak(id, principal.getUsername());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/update-streak")
    public ResponseEntity<StreakDto> updateStreak(@RequestBody @Valid StreakDto streakDto, @AuthenticationPrincipal Principal principal) {
        streakService.updateStreak(streakDto, principal.getUsername());
        return ResponseEntity.ok(streakDto);
    }
}
