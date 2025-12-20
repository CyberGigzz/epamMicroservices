package com.gym.crm.dao;

import com.gym.crm.model.Trainer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TrainerDAO {

    @PersistenceContext
    private EntityManager entityManager;

    public Trainer save(Trainer trainer) {
        if (trainer.getId() == null) {
            entityManager.persist(trainer);
            return trainer;
        } else {
            return entityManager.merge(trainer);
        }
    }

    public Optional<Trainer> findById(Long id) {
        Trainer trainer = entityManager.find(Trainer.class, id);
        return Optional.ofNullable(trainer);
    }

    public Optional<Trainer> findByUsername(String username) {
        try {
            Trainer trainer = entityManager.createQuery("SELECT t FROM Trainer t WHERE t.username = :username", Trainer.class)
                    .setParameter("username", username)
                    .getSingleResult();
            return Optional.of(trainer);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
    
    public List<Trainer> findAll() {
        return entityManager.createQuery("SELECT t FROM Trainer t", Trainer.class).getResultList();
    }
    
}