package com.gym.crm.mapper;

import com.gym.crm.dto.training.TrainingTypeDto;
import com.gym.crm.model.TrainingType;
import org.springframework.stereotype.Component;

@Component
public class TrainingTypeMapper {

    public TrainingTypeDto toDto(TrainingType trainingType) {
        TrainingTypeDto dto = new TrainingTypeDto();
        dto.setId(trainingType.getId());
        dto.setTrainingTypeName(trainingType.getTrainingTypeName());
        return dto;
    }
}