package com.gym.trainer_workload_service.service;

import com.gym.trainer_workload_service.dto.TrainerSummary;
import com.gym.trainer_workload_service.dto.WorkloadRequest;
import com.gym.trainer_workload_service.model.TrainerWorkload;
import com.gym.trainer_workload_service.model.TrainerWorkloadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkloadServiceTest {

    @Mock
    private TrainerWorkloadRepository repository;

    @InjectMocks
    private WorkloadService workloadService;

    private WorkloadRequest addRequest;
    private WorkloadRequest deleteRequest;

    @BeforeEach
    void setUp() {
        addRequest = WorkloadRequest.builder()
                .trainerUsername("john.doe")
                .trainerFirstName("John")
                .trainerLastName("Doe")
                .isActive(true)
                .trainingDate(LocalDate.of(2025, 1, 15))
                .trainingDuration(60)
                .actionType(WorkloadRequest.ActionType.ADD)
                .build();

        deleteRequest = WorkloadRequest.builder()
                .trainerUsername("john.doe")
                .trainerFirstName("John")
                .trainerLastName("Doe")
                .isActive(true)
                .trainingDate(LocalDate.of(2025, 1, 15))
                .trainingDuration(30)
                .actionType(WorkloadRequest.ActionType.DELETE)
                .build();
    }

    @Test
    void processWorkload_AddNewTrainer_CreatesNewDocument() {
        when(repository.findByTrainerUsername("john.doe")).thenReturn(Optional.empty());
        when(repository.save(any(TrainerWorkload.class))).thenAnswer(i -> i.getArgument(0));

        workloadService.processWorkload(addRequest);

        ArgumentCaptor<TrainerWorkload> captor = ArgumentCaptor.forClass(TrainerWorkload.class);
        verify(repository).save(captor.capture());

        TrainerWorkload saved = captor.getValue();
        assertEquals("john.doe", saved.getTrainerUsername());
        assertEquals("John", saved.getTrainerFirstName());
        assertEquals("Doe", saved.getTrainerLastName());
        assertTrue(saved.getIsActive());
        assertEquals(60, saved.getDuration(2025, 1));
    }

    @Test
    void processWorkload_AddExistingTrainer_UpdatesDuration() {
        TrainerWorkload existing = TrainerWorkload.builder()
                .id("123")
                .trainerUsername("john.doe")
                .trainerFirstName("John")
                .trainerLastName("Doe")
                .isActive(true)
                .years(new HashMap<>())
                .build();
        existing.addDuration(2025, 1, 60);

        when(repository.findByTrainerUsername("john.doe")).thenReturn(Optional.of(existing));
        when(repository.save(any(TrainerWorkload.class))).thenAnswer(i -> i.getArgument(0));

        workloadService.processWorkload(addRequest);

        ArgumentCaptor<TrainerWorkload> captor = ArgumentCaptor.forClass(TrainerWorkload.class);
        verify(repository).save(captor.capture());

        TrainerWorkload saved = captor.getValue();
        assertEquals(120, saved.getDuration(2025, 1)); // 60 + 60
    }

    @Test
    void processWorkload_DeleteExistingDuration_SubtractsDuration() {
        TrainerWorkload existing = TrainerWorkload.builder()
                .id("123")
                .trainerUsername("john.doe")
                .trainerFirstName("John")
                .trainerLastName("Doe")
                .isActive(true)
                .years(new HashMap<>())
                .build();
        existing.addDuration(2025, 1, 60);

        when(repository.findByTrainerUsername("john.doe")).thenReturn(Optional.of(existing));
        when(repository.save(any(TrainerWorkload.class))).thenAnswer(i -> i.getArgument(0));

        workloadService.processWorkload(deleteRequest);

        ArgumentCaptor<TrainerWorkload> captor = ArgumentCaptor.forClass(TrainerWorkload.class);
        verify(repository).save(captor.capture());

        TrainerWorkload saved = captor.getValue();
        assertEquals(30, saved.getDuration(2025, 1)); // 60 - 30
    }

    @Test
    void processWorkload_DeleteMoreThanAvailable_DoesNotGoNegative() {
        TrainerWorkload existing = TrainerWorkload.builder()
                .id("123")
                .trainerUsername("john.doe")
                .trainerFirstName("John")
                .trainerLastName("Doe")
                .isActive(true)
                .years(new HashMap<>())
                .build();
        existing.addDuration(2025, 1, 20);

        WorkloadRequest largeDelete = WorkloadRequest.builder()
                .trainerUsername("john.doe")
                .trainerFirstName("John")
                .trainerLastName("Doe")
                .isActive(true)
                .trainingDate(LocalDate.of(2025, 1, 15))
                .trainingDuration(100) // More than available
                .actionType(WorkloadRequest.ActionType.DELETE)
                .build();

        when(repository.findByTrainerUsername("john.doe")).thenReturn(Optional.of(existing));
        when(repository.save(any(TrainerWorkload.class))).thenAnswer(i -> i.getArgument(0));

        workloadService.processWorkload(largeDelete);

        ArgumentCaptor<TrainerWorkload> captor = ArgumentCaptor.forClass(TrainerWorkload.class);
        verify(repository).save(captor.capture());

        TrainerWorkload saved = captor.getValue();
        assertEquals(0, saved.getDuration(2025, 1)); // Cannot go negative
    }

    @Test
    void processWorkload_AddMultipleMonths_StoresEachMonth() {
        when(repository.findByTrainerUsername("john.doe")).thenReturn(Optional.empty());
        when(repository.save(any(TrainerWorkload.class))).thenAnswer(i -> i.getArgument(0));

        // Add for January
        workloadService.processWorkload(addRequest);

        ArgumentCaptor<TrainerWorkload> captor = ArgumentCaptor.forClass(TrainerWorkload.class);
        verify(repository).save(captor.capture());
        TrainerWorkload januaryWorkload = captor.getValue();

        // Now add for February with existing document
        WorkloadRequest februaryRequest = WorkloadRequest.builder()
                .trainerUsername("john.doe")
                .trainerFirstName("John")
                .trainerLastName("Doe")
                .isActive(true)
                .trainingDate(LocalDate.of(2025, 2, 10))
                .trainingDuration(45)
                .actionType(WorkloadRequest.ActionType.ADD)
                .build();

        when(repository.findByTrainerUsername("john.doe")).thenReturn(Optional.of(januaryWorkload));

        workloadService.processWorkload(februaryRequest);

        verify(repository, times(2)).save(captor.capture());
        TrainerWorkload saved = captor.getValue();
        assertEquals(60, saved.getDuration(2025, 1));
        assertEquals(45, saved.getDuration(2025, 2));
    }

    @Test
    void getTrainerSummary_ExistingTrainer_ReturnsSummary() {
        Map<String, Map<String, Integer>> years = new HashMap<>();
        Map<String, Integer> months2025 = new HashMap<>();
        months2025.put("1", 60);
        months2025.put("2", 45);
        years.put("2025", months2025);

        TrainerWorkload workload = TrainerWorkload.builder()
                .id("123")
                .trainerUsername("john.doe")
                .trainerFirstName("John")
                .trainerLastName("Doe")
                .isActive(true)
                .years(years)
                .build();

        when(repository.findByTrainerUsername("john.doe")).thenReturn(Optional.of(workload));

        TrainerSummary summary = workloadService.getTrainerSummary("john.doe");

        assertNotNull(summary);
        assertEquals("john.doe", summary.getTrainerUsername());
        assertEquals("John", summary.getTrainerFirstName());
        assertEquals("Doe", summary.getTrainerLastName());
        assertTrue(summary.getTrainerStatus());
        assertEquals(1, summary.getYears().size());
        assertEquals(2025, summary.getYears().get(0).getYear());
        assertEquals(2, summary.getYears().get(0).getMonths().size());
    }

    @Test
    void getTrainerSummary_NonExistingTrainer_ReturnsNull() {
        when(repository.findByTrainerUsername("unknown")).thenReturn(Optional.empty());

        TrainerSummary summary = workloadService.getTrainerSummary("unknown");

        assertNull(summary);
    }
}
