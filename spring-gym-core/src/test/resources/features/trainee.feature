Feature: Trainee Management
  As a gym administrator
  I want to manage trainee accounts
  So that I can register and track gym members

  Background:
    Given the gym service is running

  # POSITIVE SCENARIOS - Using pre-seeded users from data.sql

  Scenario: Register a new trainee
    When I register a trainee with the following details:
      | firstName   | TestUser   |
      | lastName    | NewPerson  |
      | dateOfBirth | 1990-05-15 |
      | address     | 123 Main St|
    Then the registration should be successful
    And the response should contain a generated username
    And the response should contain a generated password

  Scenario: Get trainee profile using pre-seeded user
    Given I am authenticated as "mike.johnson"
    When I request the profile for trainee "mike.johnson"
    Then the response status should be 200
    And the profile should contain firstName "Mike"
    And the profile should contain lastName "Johnson"

  Scenario: Update trainee profile using pre-seeded user
    Given I am authenticated as "sarah.wilson"
    When I update trainee "sarah.wilson" with:
      | firstName | SarahUpdated |
      | address   | 789 New Ave  |
    Then the update should be successful
    And the profile should contain firstName "SarahUpdated"
    And the profile should contain address "789 New Ave"

  Scenario: Activate and deactivate trainee
    Given I am authenticated as "mike.johnson"
    When I deactivate trainee "mike.johnson"
    Then the trainee "mike.johnson" should be inactive
    When I activate trainee "mike.johnson"
    Then the trainee "mike.johnson" should be active

  # NEGATIVE SCENARIOS

  Scenario: Register trainee with missing required fields
    When I register a trainee with the following details:
      | firstName | |
      | lastName  | |
    Then the registration should fail with status 400

  Scenario: Get profile without authentication
    When I request the profile for trainee "mike.johnson" without authentication
    Then the response status should be 403

  Scenario: Get non-existent trainee profile
    Given I am authenticated as "mike.johnson"
    When I request the profile for trainee "nonexistent.user"
    Then the response status should be 404

  Scenario: Update trainee with invalid data
    Given I am authenticated as "mike.johnson"
    When I update trainee "mike.johnson" with empty required fields
    Then the update should fail with status 400
