package com.gym.crm.service;

import com.gym.crm.client.TrainerWorkloadClient;
import com.gym.crm.dao.TraineeDAO;
import com.gym.crm.dao.TrainerDAO;
import com.gym.crm.dao.TrainingDAO;
import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.Training;
import com.gym.crm.model.TrainingType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gym.crm.dto.WorkloadRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;



import java.time.LocalDate;
import java.util.Optional;

@Service
@Transactional
public class TrainingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TrainingService.class);
    private final TrainerWorkloadClient workloadClient;

    private final TrainingDAO trainingDAO;
    private final TraineeDAO traineeDAO;
    private final TrainerDAO trainerDAO;

    public TrainingService(TrainingDAO trainingDAO, TraineeDAO traineeDAO, 
                       TrainerDAO trainerDAO, TrainerWorkloadClient workloadClient) {
        this.trainingDAO = trainingDAO;
        this.traineeDAO = traineeDAO;
        this.trainerDAO = trainerDAO;
        this.workloadClient = workloadClient;
    }



    public Training addTraining(String traineeUsername, String trainerUsername, String trainingName, TrainingType trainingType, LocalDate trainingDate, int duration) {
        Optional<Trainee> traineeOpt = traineeDAO.findByUsername(traineeUsername);
        Optional<Trainer> trainerOpt = trainerDAO.findByUsername(trainerUsername);

        if (traineeOpt.isEmpty() || trainerOpt.isEmpty()) {
            LOGGER.error("Trainee or Trainer not found. Cannot create training.");
            return null;
        }

        Trainee trainee = traineeOpt.get();
        Trainer trainer = trainerOpt.get();

        Training training = new Training();
        training.setTrainee(trainee);
        training.setTrainer(trainer);
        training.setTrainingName(trainingName);
        training.setTrainingType(trainingType);
        training.setTrainingDate(trainingDate);
        training.setTrainingDuration(duration);

        trainingDAO.save(training);
        // Notify workload service
        notifyWorkloadService(trainer, trainingDate, duration, WorkloadRequest.ActionType.ADD);

        LOGGER.info("Successfully created training '{}' for trainee {}", trainingName, traineeUsername);

        return training;
    }

    @CircuitBreaker(name = "workloadService", fallbackMethod = "workloadFallback")
    private void notifyWorkloadService(Trainer trainer, LocalDate trainingDate, 
                                    int duration, WorkloadRequest.ActionType actionType) {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String authToken = "";
        String transactionId = "";
        
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            authToken = request.getHeader("Authorization");
            transactionId = request.getHeader("X-Transaction-Id");
        }

        WorkloadRequest workloadRequest = WorkloadRequest.builder()
                .trainerUsername(trainer.getUsername())
                .trainerFirstName(trainer.getFirstName())
                .trainerLastName(trainer.getLastName())
                .isActive(trainer.isActive())
                .trainingDate(trainingDate)
                .trainingDuration(duration)
                .actionType(actionType)
                .build();

        workloadClient.updateWorkload(authToken, transactionId, workloadRequest);
        LOGGER.info("Notified workload service: {} {} minutes for trainer {}", 
                actionType, duration, trainer.getUsername());
    }

    private void workloadFallback(Trainer trainer, LocalDate trainingDate, 
                              int duration, WorkloadRequest.ActionType actionType, 
                              Exception e) {
    LOGGER.warn("Circuit breaker fallback: Failed to notify workload service for trainer {}. Reason: {}", 
            trainer.getUsername(), e.getMessage());
}


}