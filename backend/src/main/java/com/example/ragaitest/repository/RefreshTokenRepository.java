package com.example.ragaitest.repository;

import com.example.ragaitest.entity.RefreshTokenEntity;
import com.example.ragaitest.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {

    Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);

    List<RefreshTokenEntity> findByUser(UserEntity user);

    @Modifying
    int deleteByExpiresAtBefore(Instant now);

    @Modifying
    void deleteByUserAndIdNot(UserEntity user, Long tokenId);

    @Modifying
    void deleteByUser(UserEntity user);

    @Modifying
    @Query("UPDATE RefreshTokenEntity r SET r.revoked = true WHERE r.user.id = :userId AND r.id != :tokenId")
    void revokeAllUserTokensExcept(Long userId, Long tokenId);
}
