package com.gym.crm.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdatePasswordRequestDto {
    @NotBlank(message = "Old password is required")
    private String oldPassword;
    @NotBlank(message = "New password is required")
    private String newPassword;
}