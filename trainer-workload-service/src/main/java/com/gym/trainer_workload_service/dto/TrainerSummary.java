package com.gym.trainer_workload_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO showing a trainer's monthly workload summary.
 *
 * Structure:
 * - Trainer info
 * - List of years
 *   - Each year has list of months
 *     - Each month has total training duration
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerSummary {

    private String trainerUsername;
    private String trainerFirstName;
    private String trainerLastName;
    private Boolean trainerStatus;
    private List<YearSummary> years;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class YearSummary {
        private Integer year;
        private List<MonthSummary> months;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthSummary {
        private Integer month;
        private Integer trainingSummaryDuration; // total minutes
    }
}