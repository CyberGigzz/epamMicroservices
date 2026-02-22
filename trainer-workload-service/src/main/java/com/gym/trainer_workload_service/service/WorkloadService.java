package com.gym.trainer_workload_service.service;

import com.gym.trainer_workload_service.dto.TrainerSummary;
import com.gym.trainer_workload_service.dto.WorkloadRequest;
import com.gym.trainer_workload_service.model.TrainerWorkload;
import com.gym.trainer_workload_service.model.TrainerWorkloadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

// @Service  // Commented out - MongoDB dependency, will be replaced with DynamoDB
@RequiredArgsConstructor
@Slf4j
public class WorkloadService {

    private final TrainerWorkloadRepository repository;

    /**
     * Process a workload request (ADD or DELETE training hours).
     */
    public void processWorkload(WorkloadRequest request) {
        int year = request.getTrainingDate().getYear();
        int month = request.getTrainingDate().getMonthValue();

        log.debug("Processing {} request for trainer {} - {} minutes for {}/{}",
                request.getActionType(),
                request.getTrainerUsername(),
                request.getTrainingDuration(),
                month, year);

        Optional<TrainerWorkload> existing = repository.findByTrainerUsername(request.getTrainerUsername());

        TrainerWorkload workload = existing.orElseGet(() -> TrainerWorkload.builder()
                .trainerUsername(request.getTrainerUsername())
                .trainerFirstName(request.getTrainerFirstName())
                .trainerLastName(request.getTrainerLastName())
                .isActive(request.getIsActive())
                .years(new HashMap<>())
                .build());

        // Update trainer info (may have changed)
        workload.setTrainerFirstName(request.getTrainerFirstName());
        workload.setTrainerLastName(request.getTrainerLastName());
        workload.setIsActive(request.getIsActive());

        if (request.getActionType() == WorkloadRequest.ActionType.ADD) {
            workload.addDuration(year, month, request.getTrainingDuration());
            log.debug("Added {} minutes for trainer {} in {}/{}",
                    request.getTrainingDuration(), request.getTrainerUsername(), month, year);
        } else {
            workload.subtractDuration(year, month, request.getTrainingDuration());
            log.debug("Subtracted {} minutes for trainer {} in {}/{}",
                    request.getTrainingDuration(), request.getTrainerUsername(), month, year);
        }

        repository.save(workload);
        log.debug("Saved workload for {} - current duration for {}/{}: {} minutes",
                request.getTrainerUsername(), month, year, workload.getDuration(year, month));
    }

    /**
     * Get summary of all workload for a trainer.
     */
    public TrainerSummary getTrainerSummary(String username) {
        Optional<TrainerWorkload> workloadOpt = repository.findByTrainerUsername(username);

        if (workloadOpt.isEmpty()) {
            return null;
        }

        TrainerWorkload workload = workloadOpt.get();

        // Convert nested Map structure to TrainerSummary DTOs
        List<TrainerSummary.YearSummary> years = workload.getYears().entrySet().stream()
                .sorted(Comparator.comparing(e -> Integer.parseInt(e.getKey())))
                .map(yearEntry -> TrainerSummary.YearSummary.builder()
                        .year(Integer.parseInt(yearEntry.getKey()))
                        .months(yearEntry.getValue().entrySet().stream()
                                .sorted(Comparator.comparing(e -> Integer.parseInt(e.getKey())))
                                .map(monthEntry -> TrainerSummary.MonthSummary.builder()
                                        .month(Integer.parseInt(monthEntry.getKey()))
                                        .trainingSummaryDuration(monthEntry.getValue())
                                        .build())
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());

        return TrainerSummary.builder()
                .trainerUsername(workload.getTrainerUsername())
                .trainerFirstName(workload.getTrainerFirstName())
                .trainerLastName(workload.getTrainerLastName())
                .trainerStatus(workload.getIsActive())
                .years(years)
                .build();
    }
}