package com.gym.trainer_workload_service.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TrainerWorkloadTest {

    private TrainerWorkload workload;

    @BeforeEach
    void setUp() {
        workload = TrainerWorkload.builder()
                .trainerUsername("john.doe")
                .trainerFirstName("John")
                .trainerLastName("Doe")
                .isActive(true)
                .build();
    }

    @Test
    void addDuration_NewYearAndMonth_CreatesBothAndAddsDuration() {
        workload.addDuration(2025, 1, 60);

        assertEquals(60, workload.getDuration(2025, 1));
        assertNotNull(workload.getYears().get("2025"));
        assertNotNull(workload.getYears().get("2025").get("1"));
    }

    @Test
    void addDuration_ExistingMonth_AccumulatesDuration() {
        workload.addDuration(2025, 1, 60);
        workload.addDuration(2025, 1, 30);

        assertEquals(90, workload.getDuration(2025, 1));
    }

    @Test
    void addDuration_DifferentMonths_StoresSeparately() {
        workload.addDuration(2025, 1, 60);
        workload.addDuration(2025, 2, 45);

        assertEquals(60, workload.getDuration(2025, 1));
        assertEquals(45, workload.getDuration(2025, 2));
    }

    @Test
    void addDuration_DifferentYears_StoresSeparately() {
        workload.addDuration(2024, 12, 100);
        workload.addDuration(2025, 1, 60);

        assertEquals(100, workload.getDuration(2024, 12));
        assertEquals(60, workload.getDuration(2025, 1));
    }

    @Test
    void subtractDuration_ExistingDuration_ReducesCorrectly() {
        workload.addDuration(2025, 1, 60);
        workload.subtractDuration(2025, 1, 20);

        assertEquals(40, workload.getDuration(2025, 1));
    }

    @Test
    void subtractDuration_MoreThanAvailable_CapsAtZero() {
        workload.addDuration(2025, 1, 30);
        workload.subtractDuration(2025, 1, 100);

        assertEquals(0, workload.getDuration(2025, 1));
    }

    @Test
    void subtractDuration_NonExistingYear_DoesNothing() {
        workload.subtractDuration(2025, 1, 60);

        assertEquals(0, workload.getDuration(2025, 1));
    }

    @Test
    void subtractDuration_NonExistingMonth_DoesNothing() {
        workload.addDuration(2025, 1, 60);
        workload.subtractDuration(2025, 2, 30);

        assertEquals(60, workload.getDuration(2025, 1));
        assertEquals(0, workload.getDuration(2025, 2));
    }

    @Test
    void getDuration_NonExistingYear_ReturnsZero() {
        assertEquals(0, workload.getDuration(2030, 1));
    }

    @Test
    void getDuration_NonExistingMonth_ReturnsZero() {
        workload.addDuration(2025, 1, 60);
        assertEquals(0, workload.getDuration(2025, 6));
    }
}
