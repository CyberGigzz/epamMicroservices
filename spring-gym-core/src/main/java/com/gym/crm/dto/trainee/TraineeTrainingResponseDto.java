package com.gym.crm.dto.trainee;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDate;

@Data
@AllArgsConstructor 
public class TraineeTrainingResponseDto {
    private String trainingName;
    private LocalDate trainingDate;
    private String trainingType;
    private Integer trainingDuration;
    private String trainerName;
}