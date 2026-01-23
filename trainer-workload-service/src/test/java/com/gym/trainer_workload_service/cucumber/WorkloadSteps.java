package com.gym.trainer_workload_service.cucumber;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gym.trainer_workload_service.dto.TrainerSummary;
import com.gym.trainer_workload_service.dto.WorkloadRequest;
import com.gym.trainer_workload_service.model.TrainerWorkload;
import com.gym.trainer_workload_service.model.TrainerWorkloadRepository;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import javax.crypto.SecretKey;
import java.time.LocalDate;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Step definitions for workload.feature scenarios.
 *
 * Each @Given, @When, @Then method maps to a line in the .feature file.
 * The regex/cucumber expressions capture parameters from the Gherkin text.
 */
public class WorkloadSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TrainerWorkloadRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${jwt.secret-key}")
    private String jwtSecret;

    private MvcResult lastResult;
    private String jwtToken;

    /**
     * Runs before each scenario - cleans up the database and generates a test JWT.
     */
    @Before
    public void setup() {
        repository.deleteAll();
        jwtToken = generateTestJwt();
    }

    /**
     * Generates a valid JWT token for test requests.
     */
    private String generateTestJwt() {
        SecretKey key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(jwtSecret));
        return Jwts.builder()
                .subject("test-user")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000)) // 1 hour
                .signWith(key)
                .compact();
    }

    // ==================== GIVEN STEPS ====================

    @Given("the workload service is running")
    public void theWorkloadServiceIsRunning() {
        // Service is running if we got here (Spring context loaded)
        assertThat(mockMvc).isNotNull();
    }

    @Given("no workload exists for trainer {string}")
    public void noWorkloadExistsForTrainer(String username) {
        repository.findByTrainerUsername(username)
                .ifPresent(w -> repository.delete(w));
    }

    @Given("trainer {string} has {int} minutes for January {int}")
    public void trainerHasMinutesForJanuary(String username, int minutes, int year) {
        createOrUpdateWorkload(username, year, 1, minutes);
    }

    @Given("trainer {string} has {int} minutes for February {int}")
    public void trainerHasMinutesForFebruary(String username, int minutes, int year) {
        createOrUpdateWorkload(username, year, 2, minutes);
    }

    private void createOrUpdateWorkload(String username, int year, int month, int minutes) {
        Optional<TrainerWorkload> existing = repository.findByTrainerUsername(username);

        TrainerWorkload workload = existing.orElseGet(() -> TrainerWorkload.builder()
                .trainerUsername(username)
                .trainerFirstName("Test")
                .trainerLastName("Trainer")
                .isActive(true)
                .years(new HashMap<>())
                .build());

        workload.getYears().computeIfAbsent(String.valueOf(year), k -> new HashMap<>());
        workload.getYears().get(String.valueOf(year)).put(String.valueOf(month), minutes);

        repository.save(workload);
    }

    // ==================== WHEN STEPS ====================

    @When("I receive a workload request to ADD {int} minutes for trainer {string} on {string}")
    public void iReceiveWorkloadRequestToAdd(int minutes, String username, String date) throws Exception {
        sendWorkloadRequest(username, minutes, LocalDate.parse(date), WorkloadRequest.ActionType.ADD);
    }

    @When("I receive a workload request to DELETE {int} minutes for trainer {string} on {string}")
    public void iReceiveWorkloadRequestToDelete(int minutes, String username, String date) throws Exception {
        sendWorkloadRequest(username, minutes, LocalDate.parse(date), WorkloadRequest.ActionType.DELETE);
    }

    @When("I request the workload summary for trainer {string}")
    public void iRequestWorkloadSummary(String username) throws Exception {
        lastResult = mockMvc.perform(get("/api/trainers/workload/" + username)
                        .header("Authorization", "Bearer " + jwtToken))
                .andReturn();
    }

    @When("I send an invalid workload request without trainer username")
    public void iSendInvalidRequestWithoutUsername() throws Exception {
        WorkloadRequest request = WorkloadRequest.builder()
                .trainerUsername(null)  // Missing required field
                .trainerFirstName("Test")
                .trainerLastName("Trainer")
                .isActive(true)
                .trainingDate(LocalDate.now())
                .trainingDuration(60)
                .actionType(WorkloadRequest.ActionType.ADD)
                .build();

        lastResult = mockMvc.perform(post("/api/trainers/workload")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();
    }

    @When("I send an invalid workload request with null date for trainer {string}")
    public void iSendInvalidRequestWithNullDate(String username) throws Exception {
        WorkloadRequest request = WorkloadRequest.builder()
                .trainerUsername(username)
                .trainerFirstName("Test")
                .trainerLastName("Trainer")
                .isActive(true)
                .trainingDate(null)  // Missing required field
                .trainingDuration(60)
                .actionType(WorkloadRequest.ActionType.ADD)
                .build();

        lastResult = mockMvc.perform(post("/api/trainers/workload")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();
    }

    @When("I send a workload request with negative duration for trainer {string}")
    public void iSendRequestWithNegativeDuration(String username) throws Exception {
        WorkloadRequest request = WorkloadRequest.builder()
                .trainerUsername(username)
                .trainerFirstName("Test")
                .trainerLastName("Trainer")
                .isActive(true)
                .trainingDate(LocalDate.now())
                .trainingDuration(-30)  // Invalid: negative
                .actionType(WorkloadRequest.ActionType.ADD)
                .build();

        lastResult = mockMvc.perform(post("/api/trainers/workload")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();
    }

    private void sendWorkloadRequest(String username, int minutes, LocalDate date,
                                      WorkloadRequest.ActionType actionType) throws Exception {
        WorkloadRequest request = WorkloadRequest.builder()
                .trainerUsername(username)
                .trainerFirstName("Test")
                .trainerLastName("Trainer")
                .isActive(true)
                .trainingDate(date)
                .trainingDuration(minutes)
                .actionType(actionType)
                .build();

        lastResult = mockMvc.perform(post("/api/trainers/workload")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();
    }

    // ==================== THEN STEPS ====================

    @Then("the workload should be saved successfully")
    public void theWorkloadShouldBeSavedSuccessfully() {
        assertThat(lastResult.getResponse().getStatus()).isEqualTo(200);
    }

    @Then("trainer {string} should have {int} minutes for January {int}")
    public void trainerShouldHaveMinutesForJanuary(String username, int expectedMinutes, int year) {
        verifyWorkloadMinutes(username, year, 1, expectedMinutes);
    }

    @Then("trainer {string} should have {int} minutes for February {int}")
    public void trainerShouldHaveMinutesForFebruary(String username, int expectedMinutes, int year) {
        verifyWorkloadMinutes(username, year, 2, expectedMinutes);
    }

    private void verifyWorkloadMinutes(String username, int year, int month, int expectedMinutes) {
        Optional<TrainerWorkload> workload = repository.findByTrainerUsername(username);
        assertThat(workload).isPresent();
        assertThat(workload.get().getDuration(year, month)).isEqualTo(expectedMinutes);
    }

    @Then("I should receive a valid summary response")
    public void iShouldReceiveValidSummaryResponse() throws Exception {
        assertThat(lastResult.getResponse().getStatus()).isEqualTo(200);
        String content = lastResult.getResponse().getContentAsString();
        TrainerSummary summary = objectMapper.readValue(content, TrainerSummary.class);
        assertThat(summary).isNotNull();
        assertThat(summary.getTrainerUsername()).isNotNull();
    }

    @Then("the summary should show {int} minutes for January {int}")
    public void theSummaryShouldShowMinutesForJanuary(int expectedMinutes, int year) throws Exception {
        verifySummaryMinutes(year, 1, expectedMinutes);
    }

    @Then("the summary should show {int} minutes for February {int}")
    public void theSummaryShouldShowMinutesForFebruary(int expectedMinutes, int year) throws Exception {
        verifySummaryMinutes(year, 2, expectedMinutes);
    }

    private void verifySummaryMinutes(int year, int month, int expectedMinutes) throws Exception {
        String content = lastResult.getResponse().getContentAsString();
        TrainerSummary summary = objectMapper.readValue(content, TrainerSummary.class);

        TrainerSummary.YearSummary yearSummary = summary.getYears().stream()
                .filter(y -> y.getYear() == year)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Year " + year + " not found in summary"));

        TrainerSummary.MonthSummary monthSummary = yearSummary.getMonths().stream()
                .filter(m -> m.getMonth() == month)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Month " + month + " not found in summary"));

        assertThat(monthSummary.getTrainingSummaryDuration()).isEqualTo(expectedMinutes);
    }

    @Then("I should receive a not found response")
    public void iShouldReceiveNotFoundResponse() {
        assertThat(lastResult.getResponse().getStatus()).isEqualTo(404);
    }

    @Then("I should receive a bad request response")
    public void iShouldReceiveBadRequestResponse() {
        assertThat(lastResult.getResponse().getStatus()).isEqualTo(400);
    }
}
