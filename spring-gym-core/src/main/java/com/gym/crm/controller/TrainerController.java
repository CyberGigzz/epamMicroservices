package com.gym.crm.controller;

import com.gym.crm.dto.auth.CredentialsDto;
import com.gym.crm.dto.trainer.TrainerProfileResponseDto;
import com.gym.crm.dto.trainer.TrainerRegistrationRequestDto;
import com.gym.crm.dto.trainer.TrainerTrainingResponseDto;
import com.gym.crm.dto.trainer.UpdateTrainerProfileRequestDto;
import com.gym.crm.exception.EntityNotFoundException;
import com.gym.crm.mapper.TrainerMapper;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.TrainingType;
import com.gym.crm.service.TrainerService;
import com.gym.crm.service.TrainingTypeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

// @RestController
// @RequestMapping("/api/trainers")
@Tag(name = "Trainer Controller", description = "Endpoints for managing trainer profiles and activities")
public class TrainerController {

    private final TrainerService trainerService;
    private final TrainingTypeService trainingTypeService;
    private final TrainerMapper trainerMapper;

    public TrainerController(TrainerService trainerService,
                             TrainingTypeService trainingTypeService,
                             TrainerMapper trainerMapper) {
        this.trainerService = trainerService;
        this.trainingTypeService = trainingTypeService;
        this.trainerMapper = trainerMapper;
    }

    @Operation(summary = "Register a new trainer (Task 2)", description = "Creates a new trainer profile and returns their generated username and password.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Trainer registered successfully",
                         content = { @Content(mediaType = "application/json",
                         schema = @Schema(implementation = CredentialsDto.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "404", description = "Specialization (TrainingType) not found")
    })
    @PostMapping("/register")
    public ResponseEntity<CredentialsDto> registerTrainer(@Valid @RequestBody TrainerRegistrationRequestDto requestDto) {

        TrainingType specialization = trainingTypeService.findById(requestDto.getSpecializationId())
                .orElseThrow(() -> new EntityNotFoundException("TrainingType not found with ID: " + requestDto.getSpecializationId()));

        CredentialsDto credentials = trainerService.createTrainerProfile(
                requestDto.getFirstName(),
                requestDto.getLastName(),
                specialization);

        return ResponseEntity.status(HttpStatus.CREATED).body(credentials);
    }

    @Operation(summary = "Get trainer profile by username (Task 8)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the trainer",
                         content = { @Content(mediaType = "application/json",
                         schema = @Schema(implementation = TrainerProfileResponseDto.class)) }),
            @ApiResponse(responseCode = "404", description = "Trainer not found")
    })
    @GetMapping("/{username}")
    public ResponseEntity<TrainerProfileResponseDto> getTrainerProfile(
            @Parameter(description = "Username of the trainer to be fetched") @PathVariable String username) {
        Trainer trainer = trainerService.selectTrainerProfileByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found with username: " + username));
        return ResponseEntity.ok(trainerMapper.toTrainerProfileResponseDto(trainer));
    }

    @Operation(summary = "Update trainer profile (Task 9)", description = "Updates trainer details. Note: Specialization is read-only and not updated via this endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trainer profile updated",
                         content = { @Content(mediaType = "application/json",
                         schema = @Schema(implementation = TrainerProfileResponseDto.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "404", description = "Trainer not found")
    })
    @PutMapping("/{username}")
    public ResponseEntity<TrainerProfileResponseDto> updateTrainerProfile(
            @Parameter(description = "Username of the trainer to be updated") @PathVariable String username, 
            @Valid @RequestBody UpdateTrainerProfileRequestDto requestDto) {

        Trainer existingTrainer = trainerService.selectTrainerProfileByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found with username: " + username));

        Trainer updatedTrainer = trainerService.updateTrainerProfile(username, requestDto.getFirstName(), requestDto.getLastName(),
                        existingTrainer.getSpecialization(), 
                        requestDto.isActive())
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found during update for username: " + username)); 

        return ResponseEntity.ok(trainerMapper.toTrainerProfileResponseDto(updatedTrainer));
    }

    @Operation(summary = "Get Trainer Trainings List (Task 13)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved training list"),
            @ApiResponse(responseCode = "404", description = "Trainer not found")
    })
    @GetMapping("/{username}/trainings")
    public ResponseEntity<List<TrainerTrainingResponseDto>> getTrainerTrainings(
            @Parameter(description = "Username of the trainer") @PathVariable String username,
            @Parameter(description = "Filter from date (YYYY-MM-DD)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "Filter to date (YYYY-MM-DD)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @Parameter(description = "Filter by trainee's name") @RequestParam(required = false) String traineeName) { 

        trainerService.selectTrainerProfileByUsername(username)
                 .orElseThrow(() -> new EntityNotFoundException("Trainer not found with username: " + username));

        List<Object[]> results = trainerService.getTrainerTrainingsList(username, fromDate, toDate, traineeName);

        List<TrainerTrainingResponseDto> response = results.stream()
                .map(r -> new TrainerTrainingResponseDto(
                        (String)r[0],     
                        (LocalDate)r[1],  
                        (String)r[2],     
                        (Integer)r[3],    
                        (String)r[4]      
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Activate or deactivate a trainer (Task 16)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trainer status updated"),
            @ApiResponse(responseCode = "404", description = "Trainer not found")
    })
    @PatchMapping("/{username}/status")
    public ResponseEntity<Void> activateDeactivateTrainer(
            @Parameter(description = "Username of the trainer") @PathVariable String username, 
            @Parameter(description = "Set to true to activate, false to deactivate") @RequestParam boolean isActive) {
        boolean updated = trainerService.activateDeactivateTrainer(username, isActive);
         if (!updated) {
             throw new EntityNotFoundException("Trainer not found with username: " + username);
        }
        return ResponseEntity.ok().build();
    }
}