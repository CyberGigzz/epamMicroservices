package com.gym.crm.mapper;

import com.gym.crm.dto.trainee.TraineeProfileResponseDto;
import com.gym.crm.dto.trainee.TrainerInfoDto;
import com.gym.crm.model.Trainee;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class TraineeMapper {
    public TraineeProfileResponseDto toTraineeProfileResponseDto(Trainee trainee) {
        TraineeProfileResponseDto dto = new TraineeProfileResponseDto();
        dto.setFirstName(trainee.getFirstName());
        dto.setLastName(trainee.getLastName());
        dto.setDateOfBirth(trainee.getDateOfBirth());
        dto.setAddress(trainee.getAddress());
        dto.setActive(trainee.isActive());
        dto.setTrainers(trainee.getTrainers().stream()
                .map(trainer -> {
                    TrainerInfoDto trainerDto = new TrainerInfoDto();
                    trainerDto.setUsername(trainer.getUsername());
                    trainerDto.setFirstName(trainer.getFirstName());
                    trainerDto.setLastName(trainer.getLastName());
                    trainerDto.setSpecialization(trainer.getSpecialization().getTrainingTypeName());
                    return trainerDto;
                })
                .collect(Collectors.toList()));
        return dto;
    }
}