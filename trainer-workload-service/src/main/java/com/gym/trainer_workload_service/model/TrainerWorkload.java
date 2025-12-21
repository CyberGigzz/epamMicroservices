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
           columnNames = {"trainer_username", "training_year", "training_month"}
       ))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerWorkload {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "trainer_username", nullable = false)
    private String trainerUsername;

    @Column(name = "trainer_first_name")
    private String trainerFirstName;

    @Column(name = "trainer_last_name")
    private String trainerLastName;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "training_year", nullable = false)
    private Integer trainingYear;

    @Column(name = "training_month", nullable = false)
    private Integer trainingMonth;

    @Column(name = "total_duration", nullable = false)
    private Integer totalDuration; // in minutes
}