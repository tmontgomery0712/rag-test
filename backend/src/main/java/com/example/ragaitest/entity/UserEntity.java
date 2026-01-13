package com.example.ragaitest.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "USERS")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_GENERATED_KEYS")
    @SequenceGenerator(
            name = "SEQ_GENERATED_KEYS",
            sequenceName = "SEQ_GENERATED_KEYS",
            allocationSize = 1
    )
    private Long id;

    @Column(name = "NAME", nullable = false)
    private String name;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StreakEntity> streaks = new ArrayList<>();

}
