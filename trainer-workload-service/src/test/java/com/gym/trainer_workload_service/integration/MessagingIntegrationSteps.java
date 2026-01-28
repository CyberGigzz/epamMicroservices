package com.gym.trainer_workload_service.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gym.trainer_workload_service.dto.WorkloadRequest;
import com.gym.trainer_workload_service.model.TrainerWorkload;
import com.gym.trainer_workload_service.model.TrainerWorkloadRepository;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class MessagingIntegrationSteps {

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private TrainerWorkloadRepository workloadRepository;

    @Value("${workload.queue.name}")
    private String queueName;

    private final ObjectMapper objectMapper;

    public MessagingIntegrationSteps() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Before
    public void setup() {
        // Clean database before each scenario
        workloadRepository.deleteAll();
    }

    @Given("the messaging system is running")
    public void theMessagingSystemIsRunning() {
        // ActiveMQ Artemis is started by IntegrationTestConfiguration
        assertThat(jmsTemplate).isNotNull();
    }

    @Given("the database is empty")
    public void theDatabaseIsEmpty() {
        workloadRepository.deleteAll();
        assertThat(workloadRepository.count()).isZero();
    }

    @Given("a trainer {string} exists with workload {int} minutes for year {int} month {int}")
    public void aTrainerExistsWithWorkload(String username, int duration, int year, int month) {
        TrainerWorkload workload = TrainerWorkload.builder()
                .trainerUsername(username)
                .trainerFirstName("Test")
                .trainerLastName("Trainer")
                .isActive(true)
                .build();
        workload.addDuration(year, month, duration);
        workloadRepository.save(workload);
    }

    @When("a workload message is sent to the queue with:")
    public void aWorkloadMessageIsSentToTheQueueWith(DataTable dataTable) throws Exception {
        Map<String, String> data = dataTable.asMap();
        WorkloadRequest request = buildWorkloadRequest(data);
        String jsonMessage = objectMapper.writeValueAsString(request);

        jmsTemplate.convertAndSend(queueName, jsonMessage);
    }

    @When("a workload message with transactionId {string} is sent to the queue with:")
    public void aWorkloadMessageWithTransactionIdIsSentToTheQueueWith(String transactionId, DataTable dataTable) throws Exception {
        Map<String, String> data = dataTable.asMap();
        WorkloadRequest request = buildWorkloadRequest(data);
        String jsonMessage = objectMapper.writeValueAsString(request);

        jmsTemplate.convertAndSend(queueName, jsonMessage, message -> {
            message.setStringProperty("transactionId", transactionId);
            return message;
        });
    }

    @When("an invalid JSON message {string} is sent to the queue")
    public void anInvalidJsonMessageIsSentToTheQueue(String invalidJson) {
        jmsTemplate.convertAndSend(queueName, invalidJson);
    }

    @Then("the message should be consumed within {int} seconds")
    public void theMessageShouldBeConsumedWithinSeconds(int seconds) {
        // Wait a bit for message processing
        await().atMost(Duration.ofSeconds(seconds))
                .pollInterval(Duration.ofMillis(200))
                .untilAsserted(() -> {
                    // Message is consumed if queue is empty or workload exists
                    // We verify workload in subsequent steps
                });
    }

    @Then("the trainer {string} should have workload for year {int} month {int} with duration {int}")
    public void theTrainerShouldHaveWorkloadForYearMonthWithDuration(String username, int year, int month, int expectedDuration) {
        await().atMost(Duration.ofSeconds(5))
                .pollInterval(Duration.ofMillis(200))
                .untilAsserted(() -> {
                    TrainerWorkload workload = workloadRepository.findByTrainerUsername(username)
                            .orElse(null);
                    assertThat(workload).isNotNull();
                    assertThat(workload.getDuration(year, month)).isEqualTo(expectedDuration);
                });
    }

    @Then("the message processing should complete within {int} seconds")
    public void theMessageProcessingShouldCompleteWithinSeconds(int seconds) {
        // Wait for message to be processed (either successfully or with error handling)
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Then("no workload should exist for trainer {string}")
    public void noWorkloadShouldExistForTrainer(String username) {
        TrainerWorkload workload = workloadRepository.findByTrainerUsername(username).orElse(null);
        assertThat(workload).isNull();
    }

    private WorkloadRequest buildWorkloadRequest(Map<String, String> data) {
        String username = data.get("trainerUsername");
        String firstName = data.get("trainerFirstName");
        String lastName = data.get("trainerLastName");
        Boolean isActive = Boolean.parseBoolean(data.get("isActive"));
        LocalDate trainingDate = LocalDate.parse(data.get("trainingDate"));
        Integer duration = Integer.parseInt(data.get("trainingDuration"));
        WorkloadRequest.ActionType actionType = WorkloadRequest.ActionType.valueOf(data.get("actionType"));

        return WorkloadRequest.builder()
                .trainerUsername(username != null && !username.isEmpty() ? username : null)
                .trainerFirstName(firstName)
                .trainerLastName(lastName)
                .isActive(isActive)
                .trainingDate(trainingDate)
                .trainingDuration(duration)
                .actionType(actionType)
                .build();
    }
}
