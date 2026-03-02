package com.example.ragaitest.service;

import com.example.ragaitest.entity.PasswordResetEntity;
import com.example.ragaitest.entity.UserEntity;
import com.example.ragaitest.repository.PasswordResetRepository;
import com.example.ragaitest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserSettingsService {

    private final UserRepository userRepository;
    private final PasswordResetRepository passwordResetRepository;
    private final PasswordEncoder passwordEncoder;

    private static final long PASSWORD_RESET_EXPIRY_MINUTES = 30;

    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        log.info("Password changed for user: {}", user.getUsername());
    }

    @Transactional
    public String forgotPassword(String username) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Delete any existing reset tokens for this user
        passwordResetRepository.deleteByUserId(user.getId());

        // Create new reset token
        String token = UUID.randomUUID().toString();
        Instant expiresAt = Instant.now().plus(Duration.ofMinutes(PASSWORD_RESET_EXPIRY_MINUTES));

        PasswordResetEntity resetEntity = PasswordResetEntity.builder()
                .user(user)
                .token(token)
                .expiresAt(expiresAt)
                .used(false)
                .build();

        passwordResetRepository.save(resetEntity);

        log.info("Password reset token created for user: {}", username);

        // Return the token (in production, this would be sent via email)
        return token;
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetEntity resetEntity = passwordResetRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid reset token"));

        if (resetEntity.isUsed()) {
            throw new RuntimeException("Reset token has already been used");
        }

        if (resetEntity.getExpiresAt().isBefore(Instant.now())) {
            throw new RuntimeException("Reset token has expired");
        }

        UserEntity user = resetEntity.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Mark token as used and delete
        resetEntity.setUsed(true);
        passwordResetRepository.delete(resetEntity);

        log.info("Password reset successful for user: {}", user.getUsername());
    }

    @Transactional
    public void updateUsername(Long userId, String newUsername) {
        // Check if username is already taken
        if (userRepository.existsByUsername(newUsername)) {
            throw new RuntimeException("Username already exists");
        }

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setUsername(newUsername);
        userRepository.save(user);

        log.info("Username changed for user id: {} to: {}", userId, newUsername);
    }
}
