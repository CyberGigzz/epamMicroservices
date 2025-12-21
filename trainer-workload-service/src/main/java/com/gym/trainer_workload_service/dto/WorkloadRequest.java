package com.gym.trainer_workload_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Request DTO for adding/deleting trainer workload.
 *
 * This is received from the main gym service whenever
 * a training session is created or cancelled.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkloadRequest {

    @NotBlank(message = "Trainer username is required")
    private String trainerUsername;

    @NotBlank(message = "Trainer first name is required")
    private String trainerFirstName;

    @NotBlank(message = "Trainer last name is required")
    private String trainerLastName;

    @NotNull(message = "Active status is required")
    private Boolean isActive;

    @NotNull(message = "Training date is required")
    private LocalDate trainingDate;

    @NotNull(message = "Training duration is required")
    @Positive(message = "Training duration must be positive")
    private Integer trainingDuration; // in minutes

    @NotNull(message = "Action type is required")
    private ActionType actionType;

    public enum ActionType {
        ADD,    // Training was created
        DELETE  // Training was cancelled
    }
}