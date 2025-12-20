package com.gym.crm.service;

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

import java.time.LocalDate;
import java.util.Optional;

@Service
@Transactional
public class TrainingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TrainingService.class);

    private final TrainingDAO trainingDAO;
    private final TraineeDAO traineeDAO;
    private final TrainerDAO trainerDAO;

    public TrainingService(TrainingDAO trainingDAO, TraineeDAO traineeDAO, TrainerDAO trainerDAO) {
        this.trainingDAO = trainingDAO;
        this.traineeDAO = traineeDAO;
        this.trainerDAO = trainerDAO;
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
        LOGGER.info("Successfully created training '{}' for trainee {}", trainingName, traineeUsername);

        return training;
    }
}