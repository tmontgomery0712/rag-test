package com.example.ragaitest.repository;

import com.example.ragaitest.dto.StreakDtoProjection;
import com.example.ragaitest.entity.StreakEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StreakRepository extends JpaRepository<StreakEntity, Long> {

    List<StreakEntity> findByUserId(Long userId);
    void deleteAllByNameContainingIgnoreCase(String name);
}