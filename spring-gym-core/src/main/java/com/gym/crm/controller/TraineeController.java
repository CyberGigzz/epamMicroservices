package com.gym.crm.controller;

import com.gym.crm.dto.auth.CredentialsDto;
import com.gym.crm.dto.trainee.TraineeProfileResponseDto;
import com.gym.crm.dto.trainee.TraineeRegistrationRequestDto;
import com.gym.crm.dto.trainee.TraineeTrainingResponseDto;
import com.gym.crm.dto.trainee.TrainerInfoDto;
import com.gym.crm.dto.trainee.UpdateTraineeProfileRequestDto;
import com.gym.crm.dto.trainee.UpdateTraineeTrainersRequestDto;
import com.gym.crm.exception.EntityNotFoundException; 
import com.gym.crm.mapper.TraineeMapper;
import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.service.TraineeService;

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

@RestController
@RequestMapping("/api/trainees")
@Tag(name = "Trainee Controller", description = "Endpoints for managing trainee profiles and activities")
public class TraineeController {

    private final TraineeService traineeService;
    private final TraineeMapper traineeMapper;

    public TraineeController(TraineeService traineeService, TraineeMapper traineeMapper) {
        this.traineeService = traineeService;
        this.traineeMapper = traineeMapper;
    }

    @Operation(summary = "Get a list of all trainees")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    @GetMapping
    public ResponseEntity<List<TraineeProfileResponseDto>> getAllTrainees() {
        List<Trainee> trainees = traineeService.findAllTrainees();
        List<TraineeProfileResponseDto> responseDtos = trainees.stream()
                .map(traineeMapper::toTraineeProfileResponseDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDtos);
    }

    @Operation(summary = "Register a new trainee (Task 1)", description = "Creates a new trainee profile and returns their generated username and password.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Trainee registered successfully",
                         content = { @Content(mediaType = "application/json",
                         schema = @Schema(implementation = CredentialsDto.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid request body (validation error)")
    })
    @PostMapping("/register")
    public ResponseEntity<CredentialsDto> registerTrainee(@Valid @RequestBody TraineeRegistrationRequestDto requestDto) {
        CredentialsDto credentials = traineeService.createTraineeProfile(
                requestDto.getFirstName(),
                requestDto.getLastName(),
                requestDto.getDateOfBirth(),
                requestDto.getAddress());

        return ResponseEntity.status(HttpStatus.CREATED).body(credentials);
    }

    @Operation(summary = "Get trainee profile by username (Task 5)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the trainee",
                         content = { @Content(mediaType = "application/json",
                         schema = @Schema(implementation = TraineeProfileResponseDto.class)) }),
            @ApiResponse(responseCode = "404", description = "Trainee not found")
    })
    @GetMapping("/{username}")
    public ResponseEntity<TraineeProfileResponseDto> getTraineeProfile(
            @Parameter(description = "Username of the trainee to be fetched") @PathVariable String username) {
        Trainee trainee = traineeService.selectTraineeProfileByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found with username: " + username));
        return ResponseEntity.ok(traineeMapper.toTraineeProfileResponseDto(trainee));
    }

    @Operation(summary = "Update trainee profile (Task 6)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trainee profile updated",
                         content = { @Content(mediaType = "application/json",
                         schema = @Schema(implementation = TraineeProfileResponseDto.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "404", description = "Trainee not found")
    })
    @PutMapping("/{username}")
    public ResponseEntity<TraineeProfileResponseDto> updateTraineeProfile(
            @Parameter(description = "Username of the trainee to be updated") @PathVariable String username, 
            @Valid @RequestBody UpdateTraineeProfileRequestDto requestDto) {
        Trainee updatedTrainee = traineeService.updateTraineeProfile(username, requestDto.getFirstName(), requestDto.getLastName(),
                        requestDto.getDateOfBirth(), requestDto.getAddress(), requestDto.isActive())
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found with username: " + username));
        return ResponseEntity.ok(traineeMapper.toTraineeProfileResponseDto(updatedTrainee));
    }

    @Operation(summary = "Delete trainee profile (Task 7)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trainee deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Trainee not found")
    })
    @DeleteMapping("/{username}")
    public ResponseEntity<Void> deleteTraineeProfile(
            @Parameter(description = "Username of the trainee to be deleted") @PathVariable String username) {
        boolean deleted = traineeService.deleteTraineeProfileByUsername(username);
        if (!deleted) {
             throw new EntityNotFoundException("Trainee not found with username: " + username);
        }
        return ResponseEntity.ok().build();

    }

    @Operation(summary = "Get not assigned on trainee active trainers (Task 10)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list"),
            @ApiResponse(responseCode = "404", description = "Trainee not found")
    })
    @GetMapping("/{username}/trainers/unassigned")
    public ResponseEntity<List<TrainerInfoDto>> getUnassignedTrainers(
            @Parameter(description = "Username of the trainee") @PathVariable String username) {
         traineeService.selectTraineeProfileByUsername(username)
                 .orElseThrow(() -> new EntityNotFoundException("Trainee not found with username: " + username));

        List<Trainer> trainers = traineeService.getUnassignedTrainersForTrainee(username);
        List<TrainerInfoDto> response = trainers.stream().map(t -> {
            TrainerInfoDto dto = new TrainerInfoDto();
            dto.setUsername(t.getUsername());
            dto.setFirstName(t.getFirstName());
            dto.setLastName(t.getLastName());
            if (t.getSpecialization() != null) {
                dto.setSpecialization(t.getSpecialization().getTrainingTypeName());
            }
            return dto;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update Trainee's Trainer List (Task 11)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trainer list updated",
                         content = { @Content(mediaType = "application/json",
                         schema = @Schema(implementation = TrainerInfoDto.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "404", description = "Trainee not found")
    })
    @PutMapping("/{username}/trainers")
    public ResponseEntity<List<TrainerInfoDto>> updateTraineeTrainers(
            @Parameter(description = "Username of the trainee") @PathVariable String username, 
            @Valid @RequestBody UpdateTraineeTrainersRequestDto requestDto) {
        List<Trainer> updatedTrainers = traineeService.updateTraineeTrainersList(username, requestDto.getTrainerUsernames())
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found with username: " + username));

        List<TrainerInfoDto> response = updatedTrainers.stream().map(t -> {
            TrainerInfoDto dto = new TrainerInfoDto();
            dto.setUsername(t.getUsername());
            dto.setFirstName(t.getFirstName());
            dto.setLastName(t.getLastName());
            if (t.getSpecialization() != null) {
                dto.setSpecialization(t.getSpecialization().getTrainingTypeName());
            }
            return dto;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Trainee Trainings List (Task 12)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved training list"),
            @ApiResponse(responseCode = "404", description = "Trainee not found")
    })
    @GetMapping("/{username}/trainings")
    public ResponseEntity<List<TraineeTrainingResponseDto>> getTraineeTrainings(
            @Parameter(description = "Username of the trainee") @PathVariable String username,
            @Parameter(description = "Filter from date (YYYY-MM-DD)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "Filter to date (YYYY-MM-DD)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @Parameter(description = "Filter by trainer's name") @RequestParam(required = false) String trainerName,
            @Parameter(description = "Filter by training type name") @RequestParam(required = false) String trainingType) {

        traineeService.selectTraineeProfileByUsername(username)
                 .orElseThrow(() -> new EntityNotFoundException("Trainee not found with username: " + username));

        List<Object[]> results = traineeService.getTraineeTrainingsList(username, fromDate, toDate, trainerName, trainingType);

        List<TraineeTrainingResponseDto> response = results.stream()
                .map(r -> new TraineeTrainingResponseDto(
                        (String)r[0],      
                        (LocalDate)r[1],   
                        (String)r[2],      
                        (Integer)r[3],     
                        (String)r[4]       
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Activate or deactivate a trainee (Task 15)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trainee status updated"),
            @ApiResponse(responseCode = "404", description = "Trainee not found")
    })
    @PatchMapping("/{username}/status")
    public ResponseEntity<Void> activateDeactivateTrainee(
            @Parameter(description = "Username of the trainee") @PathVariable String username, 
            @Parameter(description = "Set to true to activate, false to deactivate") @RequestParam boolean isActive) {
        boolean updated = traineeService.activateDeactivateTrainee(username, isActive);
         if (!updated) {
             throw new EntityNotFoundException("Trainee not found with username: " + username);
        }
        return ResponseEntity.ok().build();

    }
}