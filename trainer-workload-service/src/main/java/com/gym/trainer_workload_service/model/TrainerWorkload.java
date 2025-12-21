package com.gym.trainer_workload_service.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity storing monthly workload for each trainer.
 *
 * One row per trainer per month.
 * Example: "john.doe" worked 480 minutes in January 2025
 */
@Entity
@Table(name = "trainer_workloads",
       uniqueConstraints = @UniqueConstraint(
           columnNames = {"trainerUsername", "year", "month"}
       ))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerWorkload {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String trainerUsername;

    private String trainerFirstName;

    private String trainerLastName;

    private Boolean isActive;

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false)
    private Integer month;

    @Column(nullable = false)
    private Integer totalDuration; // in minutes
}
