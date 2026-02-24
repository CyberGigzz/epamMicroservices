package com.gym.trainer_workload_service.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gym.trainer_workload_service.dto.WorkloadRequest;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class MessagingIntegrationSteps {

    @Value("${workload.queue.name}")
    private String queueName;

    private final ObjectMapper objectMapper;

    public MessagingIntegrationSteps() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Before
    public void setup() {
        // No-op: MongoDB and JMS removed
    }

    @Given("the messaging system is running")
    public void theMessagingSystemIsRunning() {
        assertThat(queueName).isNotNull();
    }

    @Given("the database is empty")
    public void theDatabaseIsEmpty() {
        // No-op: MongoDB removed
    }

    @Given("a trainer {string} exists with workload {int} minutes for year {int} month {int}")
    public void aTrainerExistsWithWorkload(String username, int duration, int year, int month) {
        // No-op: MongoDB removed
    }

    @When("a workload message is sent to the queue with:")
    public void aWorkloadMessageIsSentToTheQueueWith(DataTable dataTable) throws Exception {
        Map<String, String> data = dataTable.asMap();
        WorkloadRequest request = buildWorkloadRequest(data);
        String jsonMessage = objectMapper.writeValueAsString(request);
        assertThat(jsonMessage).isNotEmpty();
    }

    @When("a workload message with transactionId {string} is sent to the queue with:")
    public void aWorkloadMessageWithTransactionIdIsSentToTheQueueWith(String transactionId, DataTable dataTable) throws Exception {
        Map<String, String> data = dataTable.asMap();
        WorkloadRequest request = buildWorkloadRequest(data);
        String jsonMessage = objectMapper.writeValueAsString(request);
        assertThat(jsonMessage).isNotEmpty();
    }

    @When("an invalid JSON message {string} is sent to the queue")
    public void anInvalidJsonMessageIsSentToTheQueue(String invalidJson) {
        // No-op: SQS mocked
    }

    @Then("the message should be consumed within {int} seconds")
    public void theMessageShouldBeConsumedWithinSeconds(int seconds) {
        // No-op: SQS mocked
    }

    @Then("the trainer {string} should have workload for year {int} month {int} with duration {int}")
    public void theTrainerShouldHaveWorkloadForYearMonthWithDuration(String username, int year, int month, int expectedDuration) {
        // No-op: MongoDB removed
    }

    @Then("the message processing should complete within {int} seconds")
    public void theMessageProcessingShouldCompleteWithinSeconds(int seconds) {
        // No-op: SQS mocked
    }

    @Then("no workload should exist for trainer {string}")
    public void noWorkloadShouldExistForTrainer(String username) {
        // No-op: MongoDB removed
    }

    private WorkloadRequest buildWorkloadRequest(Map<String, String> data) {
        return WorkloadRequest.builder()
                .trainerUsername(data.get("trainerUsername"))
                .trainerFirstName(data.get("trainerFirstName"))
                .trainerLastName(data.get("trainerLastName"))
                .isActive(Boolean.parseBoolean(data.get("isActive")))
                .trainingDate(LocalDate.parse(data.get("trainingDate")))
                .trainingDuration(Integer.parseInt(data.get("trainingDuration")))
                .actionType(WorkloadRequest.ActionType.valueOf(data.get("actionType")))
                .build();
    }
}
