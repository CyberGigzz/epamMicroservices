package com.gym.crm.dao;

import com.gym.crm.model.Trainee;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// @Repository
public class TraineeDAO {

    @PersistenceContext
    private EntityManager entityManager;

    public Trainee save(Trainee trainee) {
        if (trainee.getId() == null) {
            entityManager.persist(trainee);
            return trainee;
        } else {
            return entityManager.merge(trainee);
        }
    }

    public Optional<Trainee> findById(Long id) {
        Trainee trainee = entityManager.find(Trainee.class, id);
        return Optional.ofNullable(trainee);
    }

    public Optional<Trainee> findByUsername(String username) {
        try {
            Trainee trainee = entityManager.createQuery("SELECT t FROM Trainee t WHERE t.username = :username", Trainee.class)
                    .setParameter("username", username)
                    .getSingleResult();
            return Optional.of(trainee);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public List<Trainee> findAll() {
        return entityManager.createQuery("SELECT t FROM Trainee t", Trainee.class).getResultList();
    }

    public void delete(Trainee trainee) {
        if (entityManager.contains(trainee)) {
            entityManager.remove(trainee);
        } else {
            Trainee managedTrainee = entityManager.merge(trainee);
            entityManager.remove(managedTrainee);
        }
    }

    
}