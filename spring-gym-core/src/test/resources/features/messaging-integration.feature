@integration
Feature: Training Message Producer Integration
  As a gym service
  I want to send messages to the queue when trainings are created
  So that the workload service can update trainer workloads

  Background:
    Given the gym service is running with messaging enabled
    And I am authenticated as user "john.smith"

  # POSITIVE SCENARIOS

  Scenario: Message sent to queue when training is created
    When I create a training with:
      | traineeUsername  | mike.johnson        |
      | trainerUsername  | john.smith          |
      | trainingName     | Morning Strength    |
      | trainingDate     | 2024-03-15          |
      | trainingDuration | 60                  |
      | trainingTypeId   | 2                   |
    Then the training should be created successfully
    And a message should be sent to the workload queue
    And the message should contain trainerUsername "john.smith"
    And the message should contain actionType "ADD"
    And the message should contain trainingDuration 60

  Scenario: Message contains correct trainer information
    When I create a training with:
      | traineeUsername  | sarah.wilson        |
      | trainerUsername  | jane.doe            |
      | trainingName     | Yoga Session        |
      | trainingDate     | 2024-04-20          |
      | trainingDuration | 45                  |
      | trainingTypeId   | 3                   |
    Then the training should be created successfully
    And a message should be sent to the workload queue
    And the message should contain trainerUsername "jane.doe"
    And the message should contain trainerFirstName "Jane"
    And the message should contain trainerLastName "Doe"

  Scenario: Message contains transaction ID for tracing
    When I create a training with:
      | traineeUsername  | mike.johnson        |
      | trainerUsername  | john.smith          |
      | trainingName     | Evening Cardio      |
      | trainingDate     | 2024-05-10          |
      | trainingDuration | 30                  |
      | trainingTypeId   | 1                   |
    Then the training should be created successfully
    And a message should be sent to the workload queue
    And the message should have a transactionId property

  # NEGATIVE SCENARIOS

  Scenario: No message sent when training creation fails due to invalid trainee
    When I create a training with:
      | traineeUsername  | nonexistent.trainee |
      | trainerUsername  | john.smith          |
      | trainingName     | Test Training       |
      | trainingDate     | 2024-06-01          |
      | trainingDuration | 60                  |
      | trainingTypeId   | 2                   |
    Then the training creation should fail with status 404
    And no message should be sent to the workload queue

  Scenario: No message sent when training creation fails due to invalid trainer
    When I create a training with:
      | traineeUsername  | mike.johnson        |
      | trainerUsername  | nonexistent.trainer |
      | trainingName     | Test Training       |
      | trainingDate     | 2024-06-01          |
      | trainingDuration | 60                  |
      | trainingTypeId   | 2                   |
    Then the training creation should fail with status 404
    And no message should be sent to the workload queue
