package com.gym.crm.model;

import jakarta.persistence.*; 

@Entity
@Table(name = "training_types")
public class TrainingType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "training_type_name", nullable = false)
    private String trainingTypeName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTrainingTypeName() {
        return trainingTypeName;
    }

    public void setTrainingTypeName(String trainingTypeName) {
        this.trainingTypeName = trainingTypeName;
    }

    @Override
    public String toString() {
        return "TrainingType{" +
                "id=" + id +
                ", trainingTypeName='" + trainingTypeName + '\'' +
                '}';
    }
}