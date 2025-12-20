package com.gym.crm.dao;

import com.gym.crm.model.Training;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class TrainingDAO {

    @PersistenceContext
    private EntityManager entityManager;

    public Training save(Training training) {
        if (training.getId() == null) {
            entityManager.persist(training);
            return training;
        } else {
            return entityManager.merge(training);
        }
    }

    public Optional<Training> findById(Long id) {
        Training training = entityManager.find(Training.class, id);
        return Optional.ofNullable(training);
    }
}