package com.example.ragaitest.controller;

import com.example.ragaitest.dto.AuthDtos;
import com.example.ragaitest.dto.AuthDtos.AuthResponse;
import com.example.ragaitest.dto.AuthDtos.ErrorResponse;
import com.example.ragaitest.dto.AuthDtos.RefreshResponse;
import com.example.ragaitest.dto.AuthDtos.UserDto;
import com.example.ragaitest.entity.UserEntity;
import com.example.ragaitest.repository.UserRepository;
import com.example.ragaitest.security.Principal;
import com.example.ragaitest.service.JwtService;
import com.example.ragaitest.service.LoginAttemptService;
import com.example.ragaitest.service.UserSettingsService;
import com.example.ragaitest.util.CookieUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Slf4j
@RequiredArgsConstructor
public class AuthController {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CookieUtil cookieUtil;
    private final LoginAttemptService loginAttemptService;
    private final UserSettingsService userSettingsService;

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @Valid @RequestBody AuthDtos.RegisterRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse response
    ) {
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ErrorResponse.builder()
                            .error("USERNAME_EXISTS")
                            .message("Username already taken")
                            .build());
        }

        UserEntity user = UserEntity.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(true)
                .locked(false)
                .loginAttempts(0)
                .build();

        userRepository.save(user);
        log.info("Registered new user: {}", user.getUsername());

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user, httpRequest.getHeader("User-Agent"));

        response.addHeader("Set-Cookie", cookieUtil.createRefreshCookie(refreshToken).toString());

        AuthResponse authResponse = AuthResponse.builder()
                .accessToken(accessToken)
                .user(toUserDto(user))
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody AuthDtos.LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse response
    ) {
        UserEntity user = userRepository.findByUsername(request.getUsername()).orElse(null);

        if (user == null) {
            log.debug("Login attempt with non-existent username: {}", request.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.builder()
                            .error("INVALID_CREDENTIALS")
                            .message("Invalid username or password")
                            .build());
        }

        if (loginAttemptService.isCurrentlyLocked(user)) {
            log.warn("Login attempt on locked account: {}", user.getUsername());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.builder()
                            .error("ACCOUNT_LOCKED")
                            .message("Account locked due to too many failed attempts")
                            .build());
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            loginAttemptService.loginFailed(user);
            
            int attemptsRemaining = loginAttemptService.getAttemptsRemaining(user);
            
            log.debug("Failed login for user: {}, attempts: {}", user.getUsername(), user.getLoginAttempts());

            ErrorResponse.ErrorResponseBuilder errorBuilder = ErrorResponse.builder()
                    .error("INVALID_CREDENTIALS")
                    .message("Invalid username or password");

            if (attemptsRemaining <= 2) {
                errorBuilder.attemptsRemaining(attemptsRemaining);
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorBuilder.build());
        }

        if (!user.isEnabled()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.builder()
                            .error("ACCOUNT_DISABLED")
                            .message("Account is disabled")
                            .build());
        }

        loginAttemptService.loginSucceeded(user);

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user, httpRequest.getHeader("User-Agent"));

        response.addHeader("Set-Cookie", cookieUtil.createRefreshCookie(refreshToken).toString());

        log.info("User logged in: {}", user.getUsername());

        AuthResponse authResponse = AuthResponse.builder()
                .accessToken(accessToken)
                .user(toUserDto(user))
                .build();

        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(
            HttpServletRequest httpRequest,
            HttpServletResponse response
    ) {
        String refreshToken = extractRefreshToken(httpRequest);
        
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.builder()
                            .error("MISSING_TOKEN")
                            .message("Refresh token not found")
                            .build());
        }

        try {
            String newRefreshToken = jwtService.rotateRefreshToken(refreshToken, httpRequest.getHeader("User-Agent"));
            UserEntity user = jwtService.getUserFromToken(newRefreshToken);
            String accessToken = jwtService.generateAccessToken(user);

            response.addHeader("Set-Cookie", cookieUtil.createRefreshCookie(newRefreshToken).toString());

            return ResponseEntity.ok(new RefreshResponse(accessToken));
        } catch (Exception e) {
            log.debug("Token refresh failed: {}", e.getMessage());
            response.addHeader("Set-Cookie", cookieUtil.clearRefreshCookie().toString());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.builder()
                            .error("INVALID_TOKEN")
                            .message("Refresh token is invalid or expired")
                            .build());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            HttpServletRequest httpRequest,
            HttpServletResponse response
    ) {
        String refreshToken = extractRefreshToken(httpRequest);
        
        if (refreshToken != null) {
            jwtService.revokeRefreshToken(refreshToken);
        }

        response.addHeader("Set-Cookie", cookieUtil.clearRefreshCookie().toString());
        log.info("User logged out");

        return ResponseEntity.ok().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            if (username == null || username.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Username is required"));
            }

            String resetToken = userSettingsService.forgotPassword(username.trim());
            
            // In production, send this token via email
            return ResponseEntity.ok(Map.of(
                "message", "Password reset token generated",
                "resetToken", resetToken
            ));
        } catch (RuntimeException e) {
            // Don't reveal if user exists or not
            return ResponseEntity.ok(Map.of(
                "message", "If the username exists, a reset token has been sent"
            ));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        try {
            String token = request.get("token");
            String newPassword = request.get("newPassword");

            if (token == null || newPassword == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Token and new password are required"));
            }

            userSettingsService.resetPassword(token, newPassword);
            return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(@AuthenticationPrincipal Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        UserEntity user = userRepository.findByUsername(principal.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(toUserDto(user));
    }

    private String extractRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookieUtil.getRefreshCookieName().equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private UserDto toUserDto(UserEntity user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .build();
    }
}
