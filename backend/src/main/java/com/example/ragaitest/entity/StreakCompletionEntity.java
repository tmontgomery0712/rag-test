package com.example.ragaitest.entity;


import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.time.LocalDate;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_DEFAULT;

@Setter
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(NON_DEFAULT)
@Entity
@Table(name = "STREAK_COMPLETION",
        uniqueConstraints = @UniqueConstraint(columnNames = {"STREAK_ID", "REFERENCE_DATE"}),
        indexes = @Index(name = "idx_streak_date", columnList = "STREAK_ID, REFERENCE_DATE")
)
public class StreakCompletionEntity extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "STREAK_ID", nullable = false)
    private StreakEntity streak;

    @Column(name = "REFERENCE_DATE")
    private LocalDate referenceDate;

    @Column(name = "COMPLETION_DATE")
    private Instant completionDate;


}
