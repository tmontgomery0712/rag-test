package com.example.ragaitest.controller;

import com.example.ragaitest.dto.UserPreferencesDto;
import com.example.ragaitest.security.Principal;
import com.example.ragaitest.service.UserPreferencesService;
import com.example.ragaitest.service.UserSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserPreferencesService userPreferencesService;
    private final UserSettingsService userSettingsService;

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("UserController is working");
    }

    @GetMapping("/preferences")
    public ResponseEntity<UserPreferencesDto> getPreferences(@AuthenticationPrincipal Principal principal) {
        if (principal == null) {
            return ResponseEntity.ok(UserPreferencesDto.builder().theme("dark").build());
        }
        return ResponseEntity.ok(userPreferencesService.getPreferences(principal.getUsername()));
    }

    @PostMapping("/preferences")
    public ResponseEntity<UserPreferencesDto> updatePreferences(
            @AuthenticationPrincipal Principal principal,
            @RequestBody UserPreferencesDto dto) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(userPreferencesService.updatePreferences(principal.getUsername(), dto));
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @AuthenticationPrincipal Principal principal,
            @RequestBody Map<String, String> request) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            String currentPassword = request.get("currentPassword");
            String newPassword = request.get("newPassword");

            if (currentPassword == null || newPassword == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Current password and new password are required"));
            }

            userSettingsService.changePassword(principal.getId(), currentPassword, newPassword);
            return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/username")
    public ResponseEntity<?> updateUsername(
            @AuthenticationPrincipal Principal principal,
            @RequestBody Map<String, String> request) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            String newUsername = request.get("username");
            if (newUsername == null || newUsername.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Username is required"));
            }

            userSettingsService.updateUsername(principal.getId(), newUsername.trim());
            return ResponseEntity.ok(Map.of("message", "Username updated successfully", "username", newUsername));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
