package com.gym.trainer_workload_service.service;

import com.gym.trainer_workload_service.dto.TrainerSummary;
import com.gym.trainer_workload_service.dto.WorkloadRequest;
import com.gym.trainer_workload_service.model.TrainerWorkload;
import com.gym.trainer_workload_service.model.TrainerWorkloadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkloadService {

    private final TrainerWorkloadRepository repository;

    /**
     * Process a workload request (ADD or DELETE training hours).
     */
    @Transactional
    public void processWorkload(WorkloadRequest request) {
        int year = request.getTrainingDate().getYear();
        int month = request.getTrainingDate().getMonthValue();

        log.debug("Processing {} request for trainer {} - {} minutes for {}/{}",
                request.getActionType(),
                request.getTrainerUsername(),
                request.getTrainingDuration(),
                month, year);

        Optional<TrainerWorkload> existing = repository
                .findByTrainerUsernameAndTrainingYearAndTrainingMonth(request.getTrainerUsername(), year, month);

        if (request.getActionType() == WorkloadRequest.ActionType.ADD) {
            handleAdd(request, existing, year, month);
        } else {
            handleDelete(request, existing);
        }
    }

    private void handleAdd(WorkloadRequest request, Optional<TrainerWorkload> existing,
                          int year, int month) {
        if (existing.isPresent()) {
            // Update existing record
            TrainerWorkload workload = existing.get();
            workload.setTotalDuration(workload.getTotalDuration() + request.getTrainingDuration());
            workload.setTrainerFirstName(request.getTrainerFirstName());
            workload.setTrainerLastName(request.getTrainerLastName());
            workload.setIsActive(request.getIsActive());
            repository.save(workload);
            log.debug("Updated workload for {} - new total: {} minutes",
                    request.getTrainerUsername(), workload.getTotalDuration());
        } else {
            // Create new record
            TrainerWorkload workload = TrainerWorkload.builder()
                    .trainerUsername(request.getTrainerUsername())
                    .trainerFirstName(request.getTrainerFirstName())
                    .trainerLastName(request.getTrainerLastName())
                    .isActive(request.getIsActive())
                    .trainingYear(year)
                    .trainingMonth(month)
                    .totalDuration(request.getTrainingDuration())
                    .build();
            repository.save(workload);
            log.debug("Created new workload record for {} - {} minutes",
                    request.getTrainerUsername(), request.getTrainingDuration());
        }
    }

    private void handleDelete(WorkloadRequest request, Optional<TrainerWorkload> existing) {
        existing.ifPresent(workload -> {
            int newDuration = workload.getTotalDuration() - request.getTrainingDuration();
            workload.setTotalDuration(Math.max(0, newDuration)); // Don't go negative
            repository.save(workload);
            log.debug("Reduced workload for {} - new total: {} minutes",
                    request.getTrainerUsername(), workload.getTotalDuration());
        });
    }

    /**
     * Get summary of all workload for a trainer.
     */
    @Transactional(readOnly = true)
    public TrainerSummary getTrainerSummary(String username) {
        List<TrainerWorkload> workloads = repository
                .findByTrainerUsernameOrderByTrainingYearAscTrainingMonthAsc(username);

        if (workloads.isEmpty()) {
            return null;
        }

        TrainerWorkload first = workloads.get(0);

        // Group by year
        Map<Integer, List<TrainerWorkload>> byYear = workloads.stream()
                .collect(Collectors.groupingBy(TrainerWorkload::getTrainingYear));

        List<TrainerSummary.YearSummary> years = byYear.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> TrainerSummary.YearSummary.builder()
                        .year(entry.getKey())
                        .months(entry.getValue().stream()
                                .sorted(Comparator.comparing(TrainerWorkload::getTrainingMonth))
                                .map(w -> TrainerSummary.MonthSummary.builder()
                                        .month(w.getTrainingMonth())
                                        .trainingSummaryDuration(w.getTotalDuration())
                                        .build())
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());

        return TrainerSummary.builder()
                .trainerUsername(first.getTrainerUsername())
                .trainerFirstName(first.getTrainerFirstName())
                .trainerLastName(first.getTrainerLastName())
                .trainerStatus(first.getIsActive())
                .years(years)
                .build();
    }
}