package com.gym.crm.dto.trainer;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateTrainerProfileRequestDto {
    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;
        
    private boolean isActive;
}