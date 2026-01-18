package com.gym.trainer_workload_service.model;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TrainerWorkloadRepository extends MongoRepository<TrainerWorkload, String> {

    /**
     * Find workload document by trainer username.
     */
    Optional<TrainerWorkload> findByTrainerUsername(String trainerUsername);
}
