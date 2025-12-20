package com.gym.crm.service;

import com.gym.crm.dao.TraineeDAO;
import com.gym.crm.dao.TrainerDAO;
import com.gym.crm.dao.TrainingDAO;
import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.Training;
import com.gym.crm.model.TrainingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingServiceTest {

    @Mock
    private TrainingDAO trainingDAO;
    @Mock
    private TraineeDAO traineeDAO;
    @Mock
    private TrainerDAO trainerDAO;

    @InjectMocks
    private TrainingService trainingService;

    private Trainee testTrainee;
    private Trainer testTrainer;
    private TrainingType testTrainingType;
    private LocalDate testDate;

    @BeforeEach
    void setUp() {
        testTrainee = new Trainee();
        testTrainee.setId(1L);
        testTrainee.setUsername("test.trainee");

        testTrainer = new Trainer();
        testTrainer.setId(2L);
        testTrainer.setUsername("test.trainer");

        testTrainingType = new TrainingType();
        testTrainingType.setId(1L);
        testTrainingType.setTrainingTypeName("Yoga");

        testDate = LocalDate.now();
    }

    @Test
    void addTraining_ShouldSucceed_WhenTraineeAndTrainerExist() {
        String traineeUsername = "test.trainee";
        String trainerUsername = "test.trainer";
        String trainingName = "Morning Yoga";
        int duration = 60;

        when(traineeDAO.findByUsername(traineeUsername)).thenReturn(Optional.of(testTrainee));
        when(trainerDAO.findByUsername(trainerUsername)).thenReturn(Optional.of(testTrainer));

        ArgumentCaptor<Training> trainingCaptor = ArgumentCaptor.forClass(Training.class);
        when(trainingDAO.save(trainingCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        Training result = trainingService.addTraining(
                traineeUsername, trainerUsername, trainingName,
                testTrainingType, testDate, duration);

        assertNotNull(result);
        assertEquals(testTrainee, result.getTrainee());
        assertEquals(testTrainer, result.getTrainer());
        assertEquals(trainingName, result.getTrainingName());
        assertEquals(testTrainingType, result.getTrainingType());
        assertEquals(testDate, result.getTrainingDate());
        assertEquals(duration, result.getTrainingDuration());

        verify(trainingDAO, times(1)).save(any(Training.class));

        Training capturedTraining = trainingCaptor.getValue();
        assertEquals(testTrainee, capturedTraining.getTrainee());
        assertEquals(testTrainer, capturedTraining.getTrainer());
        assertEquals(trainingName, capturedTraining.getTrainingName());
        assertEquals(testTrainingType, capturedTraining.getTrainingType());
        assertEquals(testDate, capturedTraining.getTrainingDate());
        assertEquals(duration, capturedTraining.getTrainingDuration());
        assertNull(capturedTraining.getId()); 
    }

    @Test
    void addTraining_ShouldReturnNull_WhenTraineeNotFound() {
        String traineeUsername = "unknown.trainee";
        String trainerUsername = "test.trainer";
        when(traineeDAO.findByUsername(traineeUsername)).thenReturn(Optional.empty());
        when(trainerDAO.findByUsername(trainerUsername)).thenReturn(Optional.of(testTrainer));

        Training result = trainingService.addTraining(
                traineeUsername, trainerUsername, "Morning Yoga",
                testTrainingType, testDate, 60);

        assertNull(result);

        verify(trainingDAO, never()).save(any(Training.class));
    }

    @Test
    void addTraining_ShouldReturnNull_WhenTrainerNotFound() {
        String traineeUsername = "test.trainee";
        String trainerUsername = "unknown.trainer";
        when(traineeDAO.findByUsername(traineeUsername)).thenReturn(Optional.of(testTrainee));
        when(trainerDAO.findByUsername(trainerUsername)).thenReturn(Optional.empty());

        Training result = trainingService.addTraining(
                traineeUsername, trainerUsername, "Morning Yoga",
                testTrainingType, testDate, 60);

        assertNull(result);

        verify(trainingDAO, never()).save(any(Training.class));
    }
}