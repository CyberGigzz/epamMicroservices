@integration
Feature: Messaging Integration
  As a system administrator
  I want the trainer workload service to process messages from the queue
  So that trainer workloads are automatically updated when trainings are created

  Background:
    Given the messaging system is running
    And the database is empty

  # POSITIVE SCENARIOS

  Scenario: Process ADD workload message from queue
    When a workload message is sent to the queue with:
      | trainerUsername   | john.smith   |
      | trainerFirstName  | John         |
      | trainerLastName   | Smith        |
      | isActive          | true         |
      | trainingDate      | 2024-03-15   |
      | trainingDuration  | 60           |
      | actionType        | ADD          |
    Then the message should be consumed within 5 seconds
    And the trainer "john.smith" should have workload for year 2024 month 3 with duration 60

  Scenario: Process multiple ADD messages for same trainer
    When a workload message is sent to the queue with:
      | trainerUsername   | jane.doe     |
      | trainerFirstName  | Jane         |
      | trainerLastName   | Doe          |
      | isActive          | true         |
      | trainingDate      | 2024-06-10   |
      | trainingDuration  | 45           |
      | actionType        | ADD          |
    And a workload message is sent to the queue with:
      | trainerUsername   | jane.doe     |
      | trainerFirstName  | Jane         |
      | trainerLastName   | Doe          |
      | isActive          | true         |
      | trainingDate      | 2024-06-20   |
      | trainingDuration  | 30           |
      | actionType        | ADD          |
    Then the message should be consumed within 5 seconds
    And the trainer "jane.doe" should have workload for year 2024 month 6 with duration 75

  Scenario: Process DELETE workload message reduces duration
    Given a trainer "mike.trainer" exists with workload 120 minutes for year 2024 month 5
    When a workload message is sent to the queue with:
      | trainerUsername   | mike.trainer |
      | trainerFirstName  | Mike         |
      | trainerLastName   | Trainer      |
      | isActive          | true         |
      | trainingDate      | 2024-05-15   |
      | trainingDuration  | 30           |
      | actionType        | DELETE       |
    Then the message should be consumed within 5 seconds
    And the trainer "mike.trainer" should have workload for year 2024 month 5 with duration 90

  Scenario: Process message with transaction ID for tracing
    When a workload message with transactionId "txn-12345" is sent to the queue with:
      | trainerUsername   | trace.test   |
      | trainerFirstName  | Trace        |
      | trainerLastName   | Test         |
      | isActive          | true         |
      | trainingDate      | 2024-07-01   |
      | trainingDuration  | 60           |
      | actionType        | ADD          |
    Then the message should be consumed within 5 seconds
    And the trainer "trace.test" should have workload for year 2024 month 7 with duration 60

  # NEGATIVE SCENARIOS

  Scenario: Invalid JSON message is handled without crashing
    When an invalid JSON message "not valid json" is sent to the queue
    Then the message processing should complete within 5 seconds
    And no workload should exist for trainer "invalid"

  Scenario: DELETE on non-existent trainer creates zero workload
    When a workload message is sent to the queue with:
      | trainerUsername   | nonexistent.trainer |
      | trainerFirstName  | Non                 |
      | trainerLastName   | Existent            |
      | isActive          | true                |
      | trainingDate      | 2024-08-01          |
      | trainingDuration  | 60                  |
      | actionType        | DELETE              |
    Then the message should be consumed within 5 seconds
    And the trainer "nonexistent.trainer" should have workload for year 2024 month 8 with duration 0
