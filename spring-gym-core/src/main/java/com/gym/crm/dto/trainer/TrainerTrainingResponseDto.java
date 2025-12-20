package com.gym.crm.dto.trainer;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class TrainerTrainingResponseDto {
    private String trainingName;
    private LocalDate trainingDate;
    private String trainingType;
    private Integer trainingDuration;
    private String traineeName; 
}