package com.gym.crm.mapper;

import com.gym.crm.dto.trainer.TraineeInfoDto;
import com.gym.crm.dto.trainer.TrainerProfileResponseDto;
import com.gym.crm.model.Trainer;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class TrainerMapper {

    public TrainerProfileResponseDto toTrainerProfileResponseDto(Trainer trainer) {
        TrainerProfileResponseDto dto = new TrainerProfileResponseDto();
        dto.setFirstName(trainer.getFirstName());
        dto.setLastName(trainer.getLastName());
        dto.setActive(trainer.isActive());
        
        if (trainer.getSpecialization() != null) {
            dto.setSpecialization(trainer.getSpecialization().getTrainingTypeName());
        }

        dto.setTrainees(trainer.getTrainees().stream()
                .map(trainee -> {
                    TraineeInfoDto traineeDto = new TraineeInfoDto();
                    traineeDto.setUsername(trainee.getUsername());
                    traineeDto.setFirstName(trainee.getFirstName());
                    traineeDto.setLastName(trainee.getLastName());
                    return traineeDto;
                })
                .collect(Collectors.toList()));
        return dto;
    }
}