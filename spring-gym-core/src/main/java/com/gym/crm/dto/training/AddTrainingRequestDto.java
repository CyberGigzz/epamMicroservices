package com.gym.crm.dto.training;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.time.LocalDate;

@Data
public class AddTrainingRequestDto {
    @NotBlank(message = "Trainee username is required")
    private String traineeUsername;

    @NotBlank(message = "Trainer username is required")
    private String trainerUsername;
    
    @NotBlank(message = "Training name is required")
    private String trainingName;
    
    @NotNull(message = "Training date is required")
    private LocalDate trainingDate;
    
    @NotNull(message = "Training duration is required")
    @Positive(message = "Duration must be positive")
    private Integer trainingDuration;
    
    @NotNull(message = "Training type ID is required")
    private Long trainingTypeId;
}