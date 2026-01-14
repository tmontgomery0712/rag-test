package com.example.ragaitest.services;

import com.example.ragaitest.dto.StreakDto;
import com.example.ragaitest.entity.StreakEntity;
import com.example.ragaitest.entity.UserEntity;
import com.example.ragaitest.repository.StreakCompletionRepository;
import com.example.ragaitest.repository.StreakRepository;
import com.example.ragaitest.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StreakService {
    private final StreakRepository streakRepository;
    private final StreakCompletionRepository streakCompletionRepository;
    private final UserRepository userRepository;

    //Implement userId when implemented
    public List<StreakDto> getStreaksByUserId() {
        return streakRepository.findStreakSummaries(1L);
    }

    @Transactional
    public StreakDto addStreak(StreakDto streakDto) {
        Optional<UserEntity> userEntity = userRepository.findById(1L);
        if(userEntity.isPresent()) {
            StreakEntity streakEntity = new StreakEntity();
            streakEntity.setUser(userEntity.get());
            streakEntity.setName(streakEntity.getName());
            streakEntity = streakRepository.save(streakEntity);
            streakDto.setId(streakEntity.getId());
        } else {
            throw new RuntimeException("User not found");
        }
        return streakDto;
    }
}