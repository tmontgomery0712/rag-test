package com.example.ragaitest.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "STREAK_COMPLETION", uniqueConstraints = @UniqueConstraint(columnNames = {"STREAK_ID", "COMPLETION_DATE"}))
public class StreakCompletionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_GENERATED_KEYS")
    @SequenceGenerator(
            name = "SEQ_GENERATED_KEYS",
            sequenceName = "SEQ_GENERATED_KEYS",
            allocationSize = 1
    )
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "STREAK_ID", nullable = false)
    private StreakEntity streak;

    @Column(name = "COMPLETION_DATE", nullable = false)
    private LocalDate completionDate;

    @Column(name = "COMPLETED")
    private boolean completed;



}
