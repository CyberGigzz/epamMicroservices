package com.gym.crm.health;

import com.gym.crm.service.TrainingTypeService;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

// @Component
public class DatabaseHealthIndicator implements HealthIndicator {

    private final TrainingTypeService trainingTypeService;

    public DatabaseHealthIndicator(TrainingTypeService trainingTypeService) {
        this.trainingTypeService = trainingTypeService;
    }

    @Override
    public Health health() {
        try {
            long count = trainingTypeService.findAll().size();
            
            return Health.up()
                         .withDetail("message", "Database connection is OK")
                         .withDetail("training_types_found", count)
                         .build();
        } catch (Exception ex) {
            return Health.down(ex) 
                         .withDetail("message", "Database connection failed")
                         .build();
        }
    }
}