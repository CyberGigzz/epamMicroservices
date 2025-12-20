package com.gym.crm.dto.trainee;

import java.time.LocalDate;
import java.util.List;

import lombok.Data;

@Data
public class TraineeProfileResponseDto {
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String address;
    private boolean isActive;
    private List<TrainerInfoDto> trainers;
}