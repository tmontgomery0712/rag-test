package com.example.ragaitest.service;

import com.example.ragaitest.entity.UserEntity;
import com.example.ragaitest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class LoginAttemptService {

    private final UserRepository userRepository;

    @Value("${app.security.max-login-attempts:5}")
    private int maxLoginAttempts;

    @Value("${app.security.lockout-duration-minutes:30}")
    private int lockoutDurationMinutes;

    @Transactional
    public void loginFailed(UserEntity user) {
        int newAttempts = user.getLoginAttempts() + 1;
        user.setLoginAttempts(newAttempts);

        if (newAttempts >= maxLoginAttempts) {
            user.setLocked(true);
            user.setLockedUntil(Instant.now().plus(lockoutDurationMinutes, ChronoUnit.MINUTES));
            log.warn("Account locked for user: {} due to {} failed attempts", user.getUsername(), newAttempts);
        }

        userRepository.save(user);
        log.debug("Login attempt {} for user: {}", newAttempts, user.getUsername());
    }

    @Transactional
    public void loginSucceeded(UserEntity user) {
        if (user.getLoginAttempts() > 0 || user.isLocked()) {
            user.setLoginAttempts(0);
            user.setLocked(false);
            user.setLockedUntil(null);
            userRepository.save(user);
            log.info("Reset login attempts for user: {}", user.getUsername());
        }
    }

    public boolean isCurrentlyLocked(UserEntity user) {
        if (!user.isLocked()) {
            return false;
        }

        if (user.getLockedUntil() != null && user.getLockedUntil().isBefore(Instant.now())) {
            log.info("Auto-unlocking expired lock for user: {}", user.getUsername());
            return false;
        }

        return true;
    }

    public Optional<Instant> getLockedUntil(UserEntity user) {
        if (isCurrentlyLocked(user) && user.getLockedUntil() != null) {
            return Optional.of(user.getLockedUntil());
        }
        return Optional.empty();
    }

    public int getAttemptsRemaining(UserEntity user) {
        return Math.max(0, maxLoginAttempts - user.getLoginAttempts());
    }

    public int getMaxLoginAttempts() {
        return maxLoginAttempts;
    }

    @Transactional
    public void unlockUser(UserEntity user) {
        user.setLoginAttempts(0);
        user.setLocked(false);
        user.setLockedUntil(null);
        userRepository.save(user);
        log.info("Manually unlocked user: {}", user.getUsername());
    }
}
