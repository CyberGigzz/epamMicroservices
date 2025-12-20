package com.gym.crm.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "trainers")
public class Trainer extends User {

    @ManyToOne
    @JoinColumn(name = "specialization_id")
    private TrainingType specialization;

    @ManyToMany(mappedBy = "trainers", fetch = FetchType.LAZY)
    private List<Trainee> trainees = new ArrayList<>();
    
    @OneToMany(mappedBy = "trainer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Training> trainings = new ArrayList<>();

    public TrainingType getSpecialization() {
        return specialization;
    }

    public void setSpecialization(TrainingType specialization) {
        this.specialization = specialization;
    }

    @Override
    public String toString() {
        return "Trainer{" +
                "id=" + getId() +
                ", firstName='" + getFirstName() + '\'' +
                ", lastName='" + getLastName() + '\'' +
                ", username='" + getUsername() + '\'' +
                ", isActive=" + isActive() +
                ", specialization=" + specialization +
                '}';
    }

    public List<Trainee> getTrainees() {
        return trainees;
    }

    public void setTrainees(List<Trainee> trainees) {
        this.trainees = trainees;
    }

    public List<Training> getTrainings() {
        return trainings;
    }

    public void setTrainings(List<Training> trainings) {
        this.trainings = trainings;
    }
}