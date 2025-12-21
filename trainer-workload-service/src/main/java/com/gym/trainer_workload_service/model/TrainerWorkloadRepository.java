package com.gym.trainer_workload_service.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainerWorkloadRepository extends JpaRepository<TrainerWorkload, Long> {

    /**
     * Find workload for a specific trainer in a specific month.
     */
    Optional<TrainerWorkload> findByTrainerUsernameAndYearAndMonth(
            String trainerUsername, Integer year, Integer month);

    /**
     * Find all workload records for a trainer (all months).
     */
    List<TrainerWorkload> findByTrainerUsernameOrderByYearAscMonthAsc(String trainerUsername);
}