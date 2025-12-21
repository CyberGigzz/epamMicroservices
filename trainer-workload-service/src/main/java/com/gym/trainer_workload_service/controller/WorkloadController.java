package com.gym.trainer_workload_service.controller;

import com.gym.trainer_workload_service.dto.TrainerSummary;
import com.gym.trainer_workload_service.dto.WorkloadRequest;
import com.gym.trainer_workload_service.service.WorkloadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trainers/workload")
@RequiredArgsConstructor
@Slf4j
public class WorkloadController {

    private final WorkloadService workloadService;

    /**
     * POST /api/trainers/workload
     *
     * Receives workload updates from the main gym service.
     * Called when a training is created (ADD) or cancelled (DELETE).
     */
    @PostMapping
    public ResponseEntity<Void> updateWorkload(
            @RequestHeader(value = "X-Transaction-Id", required = false) String transactionId,
            @Valid @RequestBody WorkloadRequest request) {

        log.info("TransactionId: {} - Received {} request for trainer: {}, duration: {} mins, date: {}",
                transactionId,
                request.getActionType(),
                request.getTrainerUsername(),
                request.getTrainingDuration(),
                request.getTrainingDate());

        workloadService.processWorkload(request);

        log.info("TransactionId: {} - Workload processed successfully", transactionId);
        return ResponseEntity.ok().build();
    }

    /**
     * GET /api/trainers/workload/{username}
     *
     * Returns the monthly workload summary for a trainer.
     */
    @GetMapping("/{username}")
    public ResponseEntity<TrainerSummary> getTrainerSummary(
            @RequestHeader(value = "X-Transaction-Id", required = false) String transactionId,
            @PathVariable String username) {

        log.info("TransactionId: {} - Getting summary for trainer: {}", transactionId, username);

        TrainerSummary summary = workloadService.getTrainerSummary(username);

        if (summary == null) {
            log.info("TransactionId: {} - No workload found for trainer: {}", transactionId, username);
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(summary);
    }
}