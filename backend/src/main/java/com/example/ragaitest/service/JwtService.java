package com.example.ragaitest.service;

import com.example.ragaitest.entity.RefreshTokenEntity;
import com.example.ragaitest.entity.UserEntity;
import com.example.ragaitest.repository.RefreshTokenRepository;
import com.example.ragaitest.repository.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class JwtService {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.access-expiration-ms}")
    private long accessExpirationMs;

    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    private byte[] signingKey;

    @PostConstruct
    public void init() {
        this.signingKey = jwtSecret.getBytes(StandardCharsets.UTF_8);
    }

    public String generateAccessToken(UserEntity user) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(accessExpirationMs);

        return Jwts.builder()
                .subject(user.getUsername())
                .claim("userId", user.getId())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(Keys.hmacShaKeyFor(signingKey))
                .compact();
    }

    @Transactional
    public String generateRefreshToken(UserEntity user, String deviceInfo) {
        String rawToken = Jwts.builder()
                .subject(user.getUsername())
                .claim("type", "refresh")
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plusMillis(refreshExpirationMs)))
                .signWith(Keys.hmacShaKeyFor(signingKey))
                .compact();

        RefreshTokenEntity refreshToken = RefreshTokenEntity.builder()
                .user(user)
                .tokenHash(hashToken(rawToken))
                .expiresAt(Instant.now().plusMillis(refreshExpirationMs))
                .deviceInfo(deviceInfo)
                .revoked(false)
                .createdBy(String.valueOf(user.getId()))
                .lastModifiedBy(String.valueOf(user.getId()))
                .build();

        refreshTokenRepository.save(refreshToken);
        log.debug("Created refresh token for user: {}", user.getUsername());

        return rawToken;
    }

    @Transactional
    public String rotateRefreshToken(String oldRawToken, String deviceInfo) {
        String oldTokenHash = hashToken(oldRawToken);
        log.info("rotateRefreshToken: looking up token with hash: {}", oldTokenHash);
        
        RefreshTokenEntity oldToken = refreshTokenRepository.findByTokenHash(oldTokenHash)
                .orElseThrow(() -> {
                    log.error("rotateRefreshToken: Token not found in database");
                    return new JwtException("Refresh token not found");
                });

        log.info("rotateRefreshToken: Found token, revoked={}, expiresAt={}", oldToken.isRevoked(), oldToken.getExpiresAt());

        if (oldToken.isRevoked()) {
            throw new JwtException("Refresh token has been revoked");
        }

        if (oldToken.getExpiresAt().isBefore(Instant.now())) {
            refreshTokenRepository.delete(oldToken);
            throw new JwtException("Refresh token has expired");
        }

        // For refresh tokens, we need to also regenerate the JWT since it contains expiry in claims
        UserEntity user = oldToken.getUser();
        
        // Delete old token and create new one with new expiry
        refreshTokenRepository.delete(oldToken);
        
        // Create new refresh token JWT with new expiry
        String newRawToken = Jwts.builder()
                .subject(user.getUsername())
                .claim("type", "refresh")
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plusMillis(refreshExpirationMs)))
                .signWith(Keys.hmacShaKeyFor(signingKey))
                .compact();

        // Save new token to database
        RefreshTokenEntity newToken = RefreshTokenEntity.builder()
                .user(user)
                .tokenHash(hashToken(newRawToken))
                .expiresAt(Instant.now().plusMillis(refreshExpirationMs))
                .deviceInfo(deviceInfo)
                .revoked(false)
                .createdBy(String.valueOf(user.getId()))
                .lastModifiedBy(String.valueOf(user.getId()))
                .build();

        refreshTokenRepository.save(newToken);
        
        log.info("rotateRefreshToken: Created new token for user: {}", user.getUsername());

        return newRawToken;
    }

    public Claims validateToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(signingKey))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new JwtException("Token has expired");
        } catch (UnsupportedJwtException e) {
            throw new JwtException("Unsupported token");
        } catch (MalformedJwtException e) {
            throw new JwtException("Malformed token");
        } catch (Exception e) {
            throw new JwtException("Invalid token: " + e.getMessage());
        }
    }

    public boolean validateRefreshToken(String rawToken) {
        String hash = hashToken(rawToken);
        Optional<RefreshTokenEntity> tokenOpt = refreshTokenRepository.findByTokenHash(hash);
        
        if (tokenOpt.isEmpty()) {
            return false;
        }

        RefreshTokenEntity token = tokenOpt.get();
        return !token.isRevoked() && token.getExpiresAt().isAfter(Instant.now());
    }

    @Transactional
    public void revokeRefreshToken(String rawToken) {
        String hash = hashToken(rawToken);
        refreshTokenRepository.findByTokenHash(hash).ifPresent(token -> {
            refreshTokenRepository.delete(token);
            log.debug("Revoked refresh token for user: {}", token.getUser().getUsername());
        });
    }

    @Transactional
    public void revokeAllUserTokens(UserEntity user) {
        refreshTokenRepository.deleteByUser(user);
        log.debug("Revoked all refresh tokens for user: {}", user.getUsername());
    }

    public String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to hash token", e);
        }
    }

    public UserEntity getUserFromToken(String token) {
        Claims claims = validateToken(token);
        String username = claims.getSubject();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    public long getRefreshTokenExpirationSeconds() {
        return refreshExpirationMs / 1000;
    }
}
