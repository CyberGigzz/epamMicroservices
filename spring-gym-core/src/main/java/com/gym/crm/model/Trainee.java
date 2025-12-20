package com.gym.crm.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "trainees")
public class Trainee extends User {

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column
    private String address;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "trainee_trainer",
        joinColumns = @JoinColumn(name = "trainee_id"),
        inverseJoinColumns = @JoinColumn(name = "trainer_id")
    )
    private List<Trainer> trainers = new ArrayList<>();
    
    @OneToMany(mappedBy = "trainee", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Training> trainings = new ArrayList<>();


    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "Trainee{" +
                "id=" + getId() +
                ", firstName='" + getFirstName() + '\'' +
                ", lastName='" + getLastName() + '\'' +
                ", username='" + getUsername() + '\'' +
                ", isActive=" + isActive() +
                ", dateOfBirth=" + dateOfBirth +
                ", address='" + address + '\'' +
                '}';
    }

    public List<Trainer> getTrainers() {
        return trainers;
    }

    public void setTrainers(List<Trainer> trainers) {
        this.trainers = trainers;
    }
    
    public List<Training> getTrainings() {
        return trainings;
    }

    public void setTrainings(List<Training> trainings) {
        this.trainings = trainings;
    }
}