package com.gym.trainer_workload_service.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.Map;

/**
 * MongoDB document storing trainer workload with nested year/month structure.
 *
 * Document structure:
 * {
 *   "_id": "...",
 *   "trainerUsername": "john.doe",
 *   "trainerFirstName": "John",
 *   "trainerLastName": "Doe",
 *   "isActive": true,
 *   "years": {
 *     "2025": {
 *       "1": 480,   // January: 480 minutes
 *       "2": 360    // February: 360 minutes
 *     }
 *   }
 * }
 */
@Document(collection = "trainer_workloads")
@CompoundIndex(name = "name_idx", def = "{'trainerFirstName': 1, 'trainerLastName': 1}")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerWorkload {

    @Id
    private String id;

    @Indexed(unique = true)
    private String trainerUsername;

    private String trainerFirstName;

    private String trainerLastName;

    private Boolean isActive;

    /**
     * Nested structure: years -> months -> totalDuration
     * Key: year as String (e.g., "2025")
     * Value: Map of month (1-12) to duration in minutes
     */
    @Builder.Default
    private Map<String, Map<String, Integer>> years = new HashMap<>();

    /**
     * Get duration for a specific year and month.
     */
    public Integer getDuration(int year, int month) {
        Map<String, Integer> yearData = years.get(String.valueOf(year));
        if (yearData == null) {
            return 0;
        }
        return yearData.getOrDefault(String.valueOf(month), 0);
    }

    /**
     * Add duration to a specific year and month.
     */
    public void addDuration(int year, int month, int duration) {
        String yearKey = String.valueOf(year);
        String monthKey = String.valueOf(month);

        years.computeIfAbsent(yearKey, k -> new HashMap<>());
        years.get(yearKey).merge(monthKey, duration, Integer::sum);
    }

    /**
     * Subtract duration from a specific year and month.
     * Duration cannot go below 0.
     */
    public void subtractDuration(int year, int month, int duration) {
        String yearKey = String.valueOf(year);
        String monthKey = String.valueOf(month);

        Map<String, Integer> yearData = years.get(yearKey);
        if (yearData != null) {
            int current = yearData.getOrDefault(monthKey, 0);
            int newDuration = Math.max(0, current - duration);
            yearData.put(monthKey, newDuration);
        }
    }
}
