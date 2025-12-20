package com.gym.crm.service;

import com.gym.crm.dao.TrainingTypeDAO;
import com.gym.crm.model.TrainingType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class TrainingTypeService {

    private final TrainingTypeDAO trainingTypeDAO;

    public TrainingTypeService(TrainingTypeDAO trainingTypeDAO) {
        this.trainingTypeDAO = trainingTypeDAO;
    }

    public List<TrainingType> findAll() {
        return trainingTypeDAO.findAll();
    }
    
    public Optional<TrainingType> findById(Long id) {
        return trainingTypeDAO.findById(id);
    }
}