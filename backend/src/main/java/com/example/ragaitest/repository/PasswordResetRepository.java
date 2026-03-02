package com.example.ragaitest.repository;

import com.example.ragaitest.entity.PasswordResetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetRepository extends JpaRepository<PasswordResetEntity, Long> {
    Optional<PasswordResetEntity> findByToken(String token);
    void deleteByUserId(Long userId);
}
