Feature: Trainer Workload Management
  As a gym system
  I want to track trainer workload hours
  So that I can monitor trainer activity per month

  Background:
    Given the workload service is running

  # ==================== POSITIVE SCENARIOS ====================

  Scenario: Add training hours for a new trainer
    Given no workload exists for trainer "john.smith"
    When I receive a workload request to ADD 60 minutes for trainer "john.smith" on "2025-01-15"
    Then the workload should be saved successfully
    And trainer "john.smith" should have 60 minutes for January 2025

  Scenario: Add training hours to existing trainer workload
    Given trainer "jane.doe" has 60 minutes for January 2025
    When I receive a workload request to ADD 30 minutes for trainer "jane.doe" on "2025-01-20"
    Then the workload should be saved successfully
    And trainer "jane.doe" should have 90 minutes for January 2025

  Scenario: Add training hours for different months
    Given trainer "mike.trainer" has 60 minutes for January 2025
    When I receive a workload request to ADD 45 minutes for trainer "mike.trainer" on "2025-02-10"
    Then the workload should be saved successfully
    And trainer "mike.trainer" should have 60 minutes for January 2025
    And trainer "mike.trainer" should have 45 minutes for February 2025

  Scenario: Delete training hours from trainer workload
    Given trainer "sarah.trainer" has 120 minutes for January 2025
    When I receive a workload request to DELETE 30 minutes for trainer "sarah.trainer" on "2025-01-15"
    Then the workload should be saved successfully
    And trainer "sarah.trainer" should have 90 minutes for January 2025

  Scenario: Get trainer workload summary
    Given trainer "summary.trainer" has 60 minutes for January 2025
    And trainer "summary.trainer" has 90 minutes for February 2025
    When I request the workload summary for trainer "summary.trainer"
    Then I should receive a valid summary response
    And the summary should show 60 minutes for January 2025
    And the summary should show 90 minutes for February 2025

  # ==================== NEGATIVE SCENARIOS ====================

  Scenario: Delete more hours than available should not go below zero
    Given trainer "negative.trainer" has 30 minutes for January 2025
    When I receive a workload request to DELETE 50 minutes for trainer "negative.trainer" on "2025-01-15"
    Then the workload should be saved successfully
    And trainer "negative.trainer" should have 0 minutes for January 2025

  Scenario: Get summary for non-existent trainer
    Given no workload exists for trainer "unknown.trainer"
    When I request the workload summary for trainer "unknown.trainer"
    Then I should receive a not found response

  Scenario: Add workload with missing trainer username
    When I send an invalid workload request without trainer username
    Then I should receive a bad request response

  Scenario: Add workload with invalid date
    When I send an invalid workload request with null date for trainer "test.trainer"
    Then I should receive a bad request response

  Scenario: Add workload with negative duration
    When I send a workload request with negative duration for trainer "test.trainer"
    Then I should receive a bad request response
