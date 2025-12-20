package com.gym.crm.dto.trainee;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class UpdateTraineeTrainersRequestDto {
    @NotEmpty(message = "Trainer usernames list cannot be empty")
    private List<String> trainerUsernames;
}