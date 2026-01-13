package com.example.ragaitest.services;

import com.example.ragaitest.dto.StreakDto;
import com.example.ragaitest.entity.UserEntity;
import com.example.ragaitest.repository.StreakCompletionRepository;
import com.example.ragaitest.repository.StreakRepository;
import com.example.ragaitest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StreakService {
    private final StreakRepository streakRepository;
    private final StreakCompletionRepository streakCompletionRepository;
    private final UserRepository userRepository;

    public List<StreakDto> getStreaks() {

        UserEntity userEntity = userRepository.findAll().getFirst();

    }


}
