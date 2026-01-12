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
@Table(name = "STREAK")
public class StreakEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_GENERATED_KEYS")
    @SequenceGenerator(
            name = "SEQ_GENERATED_KEYS",
            sequenceName = "SEQ_GENERATED_KEYS",
            allocationSize = 1
    )
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "USER_ID", nullable = false)
    private UserEntity user;

    @Column(name = "NAME", nullable = false)
    private String name;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "streak", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StreakCompletionEntity> completions = new ArrayList<>();
}
