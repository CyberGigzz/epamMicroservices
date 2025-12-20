package com.gym.crm.dao;

import com.gym.crm.model.TrainingType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TrainingTypeDAO {

    @PersistenceContext
    private EntityManager entityManager;

    public Optional<TrainingType> findById(Long id) {
        return Optional.ofNullable(entityManager.find(TrainingType.class, id));
    }

    public List<TrainingType> findAll() {
        return entityManager.createQuery("SELECT tt FROM TrainingType tt", TrainingType.class)
                .getResultList();
    }
}