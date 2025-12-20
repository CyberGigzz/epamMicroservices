package com.gym.crm.controller;

import com.gym.crm.dto.training.AddTrainingRequestDto;
import com.gym.crm.exception.EntityNotFoundException;
import com.gym.crm.model.Training;
import com.gym.crm.model.TrainingType;
import com.gym.crm.service.TrainingService;
import com.gym.crm.service.TrainingTypeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus; 
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trainings")
@Tag(name = "Training Controller", description = "Endpoints for managing trainings")
public class TrainingController {

    private final TrainingService trainingService;
    private final TrainingTypeService trainingTypeService;

    public TrainingController(TrainingService trainingService, TrainingTypeService trainingTypeService) {
        this.trainingService = trainingService;
        this.trainingTypeService = trainingTypeService;
    }

    @PostMapping
    @Operation(summary = "Add a new training (Task 14)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Training added successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body (validation error)"),
            @ApiResponse(responseCode = "404", description = "Trainee, Trainer, or TrainingType not found")
    })
    public ResponseEntity<Void> addTraining(@Valid @RequestBody AddTrainingRequestDto requestDto) {
        
        TrainingType trainingType = trainingTypeService.findById(requestDto.getTrainingTypeId())
                .orElseThrow(() -> new EntityNotFoundException("TrainingType not found with ID: " + requestDto.getTrainingTypeId())); 

        Training newTraining = trainingService.addTraining(
                requestDto.getTraineeUsername(),
                requestDto.getTrainerUsername(),
                requestDto.getTrainingName(),
                trainingType, 
                requestDto.getTrainingDate(),
                requestDto.getTrainingDuration()
        );

        if (newTraining != null) {
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } else {
            throw new EntityNotFoundException("Trainee or Trainer not found.");
        }
    }
}