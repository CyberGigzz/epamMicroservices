package com.gym.crm.service;

import com.gym.crm.dao.TraineeDAO;
import com.gym.crm.dao.TrainerDAO;
import com.gym.crm.dto.auth.CredentialsDto; 
import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder; 
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// @Service
// @Transactional
public class TraineeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TraineeService.class);
    private final UserService userService;
    private final TraineeDAO traineeDAO;
    private final TrainerDAO trainerDAO;
    private final PasswordEncoder passwordEncoder;

    private final Counter traineeRegistrationsCounter;

    @PersistenceContext
    private EntityManager entityManager;

    public TraineeService(UserService userService, TraineeDAO traineeDAO, TrainerDAO trainerDAO,
                          PasswordEncoder passwordEncoder, MeterRegistry meterRegistry) { 
        this.userService = userService;
        this.traineeDAO = traineeDAO;
        this.trainerDAO = trainerDAO;
        this.passwordEncoder = passwordEncoder;

        this.traineeRegistrationsCounter = Counter.builder("crm.trainee.registrations.total")
                .description("Total number of new trainee registrations")
                .tag("entity", "trainee") 
                .register(meterRegistry);
    }

    @Transactional(readOnly = true) 
    public List<Trainee> findAllTrainees() {
        LOGGER.info("Fetching all trainees");
        return traineeDAO.findAll();
    }

    public CredentialsDto createTraineeProfile(String firstName, String lastName, LocalDate dateOfBirth, String address) {
        Trainee trainee = new Trainee();
        trainee.setFirstName(firstName);
        trainee.setLastName(lastName);
        trainee.setDateOfBirth(dateOfBirth);
        trainee.setAddress(address);
        trainee.setActive(true);

        String username = userService.generateUsername(firstName, lastName);
        String plainPassword = userService.generatePlainPassword(); 
        String encodedPassword = userService.encodePassword(plainPassword);

        trainee.setUsername(username);
        trainee.setPassword(encodedPassword); 

        traineeDAO.save(trainee);
        LOGGER.info("Successfully created trainee with username: {}", username);

        this.traineeRegistrationsCounter.increment();

        CredentialsDto credentials = new CredentialsDto();
        credentials.setUsername(username);
        credentials.setPassword(plainPassword); 
        return credentials;
    }

    @Transactional(readOnly = true)
    public boolean checkTraineeCredentials(String username, String plainPassword) {
        Optional<Trainee> traineeOpt = traineeDAO.findByUsername(username);
        return traineeOpt.map(trainee -> passwordEncoder.matches(plainPassword, trainee.getPassword()))
                .orElse(false);
    }

    @Transactional(readOnly = true) 
    public Optional<Trainee> selectTraineeProfileByUsername(String username) {
        return traineeDAO.findByUsername(username);
    }

    public boolean changeTraineePassword(String username, String oldPassword, String newPassword) {
        Optional<Trainee> traineeOpt = traineeDAO.findByUsername(username);
        if (traineeOpt.isPresent()) {
            Trainee trainee = traineeOpt.get();
            if (passwordEncoder.matches(oldPassword, trainee.getPassword())) {
                trainee.setPassword(passwordEncoder.encode(newPassword));
                LOGGER.info("Password changed successfully for trainee: {}", username);
                return true;
            } else {
                LOGGER.warn("Authentication failed for trainee (incorrect old password): {}", username);
                return false;
            }
        }
        LOGGER.warn("Trainee not found for password change: {}", username);
        return false;
    }

    public Optional<Trainee> updateTraineeProfile(String username, String firstName, String lastName, LocalDate dateOfBirth, String address, boolean isActive) {
        Optional<Trainee> traineeOpt = traineeDAO.findByUsername(username);
        if (traineeOpt.isPresent()) {
            Trainee trainee = traineeOpt.get();
            trainee.setFirstName(firstName);
            trainee.setLastName(lastName);
            trainee.setDateOfBirth(dateOfBirth);
            trainee.setAddress(address);
            trainee.setActive(isActive);
            LOGGER.info("Trainee profile updated for: {}", username);
            return Optional.of(trainee);
        }
        LOGGER.warn("Trainee not found for profile update: {}", username);
        return Optional.empty();
    }

    public boolean activateDeactivateTrainee(String username, boolean isActive) {
        Optional<Trainee> traineeOpt = traineeDAO.findByUsername(username);
        if (traineeOpt.isPresent()) {
            Trainee trainee = traineeOpt.get();
            trainee.setActive(isActive);
            LOGGER.info("Trainee {} status set to: {}", username, isActive ? "ACTIVE" : "INACTIVE");
            return true;
        }
        LOGGER.warn("Trainee not found for status change: {}", username);
        return false;
    }

    public boolean deleteTraineeProfileByUsername(String username) {
        Optional<Trainee> traineeOpt = traineeDAO.findByUsername(username);
        if (traineeOpt.isPresent()) {
            traineeDAO.delete(traineeOpt.get());
            LOGGER.info("Trainee profile deleted for: {}", username);
            return true;
        }
        LOGGER.warn("Trainee not found for deletion: {}", username);
        return false;
    }

    @Transactional(readOnly = true) 
    public List<Object[]> getTraineeTrainingsList(String username, LocalDate fromDate, LocalDate toDate, String trainerName, String trainingType) {
        String jpql = "SELECT t.trainingName, t.trainingDate, t.trainingType.trainingTypeName, t.trainingDuration, tr.username FROM Training t JOIN t.trainer tr WHERE t.trainee.username = :username";

        if (fromDate != null) jpql += " AND t.trainingDate >= :fromDate";
        if (toDate != null) jpql += " AND t.trainingDate <= :toDate";
        if (trainerName != null && !trainerName.isEmpty()) jpql += " AND (tr.firstName = :trainerName OR tr.lastName = :trainerName)";
        if (trainingType != null && !trainingType.isEmpty()) jpql += " AND t.trainingType.trainingTypeName = :trainingType";

        TypedQuery<Object[]> query = entityManager.createQuery(jpql, Object[].class);
        query.setParameter("username", username);

        if (fromDate != null) query.setParameter("fromDate", fromDate);
        if (toDate != null) query.setParameter("toDate", toDate);
        if (trainerName != null && !trainerName.isEmpty()) query.setParameter("trainerName", trainerName);
        if (trainingType != null && !trainingType.isEmpty()) query.setParameter("trainingType", trainingType);

        return query.getResultList();
    }

    @Transactional(readOnly = true) 
    public List<Trainer> getUnassignedTrainersForTrainee(String traineeUsername) {
        Optional<Trainee> traineeOpt = traineeDAO.findByUsername(traineeUsername);
        if (traineeOpt.isEmpty()) {
            LOGGER.warn("Trainee not found for getting unassigned trainers: {}", traineeUsername);
            return List.of();
        }
        Trainee trainee = traineeOpt.get();
        List<Long> assignedTrainerIds = trainee.getTrainers().stream().map(Trainer::getId).collect(Collectors.toList());
        if (assignedTrainerIds.isEmpty()) {
            return trainerDAO.findAll(); 
        }
        return entityManager.createQuery("SELECT t FROM Trainer t WHERE t.isActive = true AND t.id NOT IN :assignedTrainerIds", Trainer.class)
                .setParameter("assignedTrainerIds", assignedTrainerIds)
                .getResultList();
    }


    public Optional<List<Trainer>> updateTraineeTrainersList(String username, List<String> trainerUsernames) {
        Optional<Trainee> traineeOpt = traineeDAO.findByUsername(username);
        if (traineeOpt.isPresent()) {
            Trainee trainee = traineeOpt.get();
            List<Trainer> trainers = entityManager.createQuery("SELECT t FROM Trainer t WHERE t.isActive = true AND t.username IN :usernames", Trainer.class)
                    .setParameter("usernames", trainerUsernames)
                    .getResultList();

            if (trainers.size() != trainerUsernames.size()) {
                 LOGGER.warn("Could not find all active trainers for usernames: {}", trainerUsernames);
            }

            trainee.setTrainers(trainers);
            LOGGER.info("Updated trainer list for trainee: {}", username);
            return Optional.of(trainee.getTrainers());
        }
        LOGGER.warn("Trainee not found for trainer list update: {}", username);
        return Optional.empty();
    }
}