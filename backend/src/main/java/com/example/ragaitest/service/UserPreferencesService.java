package com.example.ragaitest.service;

import com.example.ragaitest.dto.UserPreferencesDto;
import com.example.ragaitest.entity.UserEntity;
import com.example.ragaitest.entity.UserPreferencesEntity;
import com.example.ragaitest.repository.UserPreferencesRepository;
import com.example.ragaitest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserPreferencesService {
    private final UserPreferencesRepository userPreferencesRepository;
    private final UserRepository userRepository;

    @Transactional
    public UserPreferencesDto getPreferences(String username) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserPreferencesEntity preferences = userPreferencesRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    UserPreferencesEntity newPrefs = UserPreferencesEntity.builder()
                            .user(user)
                            .theme("dark")
                            .build();
                    return userPreferencesRepository.save(newPrefs);
                });

        return UserPreferencesDto.builder()
                .theme(preferences.getTheme())
                .build();
    }

    @Transactional
    public UserPreferencesDto updatePreferences(String username, UserPreferencesDto dto) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserPreferencesEntity preferences = userPreferencesRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    UserPreferencesEntity newPrefs = UserPreferencesEntity.builder()
                            .user(user)
                            .theme("dark")
                            .build();
                    return userPreferencesRepository.save(newPrefs);
                });

        if (dto.getTheme() != null) {
            preferences.setTheme(dto.getTheme());
        }

        userPreferencesRepository.save(preferences);

        return UserPreferencesDto.builder()
                .theme(preferences.getTheme())
                .build();
    }
}
