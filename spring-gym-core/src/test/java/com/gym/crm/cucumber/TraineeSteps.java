package com.gym.crm.cucumber;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gym.crm.dao.TraineeDAO;
import com.gym.crm.model.Trainee;
import com.gym.crm.security.JwtUtil;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

public class TraineeSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private TraineeDAO traineeDAO;

    private MvcResult lastResult;
    private String currentToken;

    @Before
    public void setup() {
        currentToken = null;
        lastResult = null;
    }

    private String generateToken(String username) {
        UserDetails userDetails = User.builder()
                .username(username)
                .password("password")
                .authorities(Collections.emptyList())
                .build();
        return jwtUtil.generateToken(userDetails);
    }

    @Given("the gym service is running")
    public void theGymServiceIsRunning() {
        // Application context is loaded by Spring
        // Training types and users are pre-loaded via data.sql
    }

    @Given("I am authenticated as {string}")
    public void iAmAuthenticatedAs(String username) {
        currentToken = generateToken(username);
    }

    @When("I register a trainee with the following details:")
    public void iRegisterATraineeWithTheFollowingDetails(DataTable dataTable) throws Exception {
        Map<String, String> data = dataTable.asMap();
        Map<String, Object> request = new HashMap<>();

        String firstName = data.get("firstName");
        String lastName = data.get("lastName");

        if (firstName != null && !firstName.isEmpty()) {
            request.put("firstName", firstName);
        }
        if (lastName != null && !lastName.isEmpty()) {
            request.put("lastName", lastName);
        }
        if (data.containsKey("dateOfBirth") && !data.get("dateOfBirth").isEmpty()) {
            request.put("dateOfBirth", data.get("dateOfBirth"));
        }
        if (data.containsKey("address") && !data.get("address").isEmpty()) {
            request.put("address", data.get("address"));
        }

        lastResult = mockMvc.perform(post("/api/trainees/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();
    }

    @When("I request the profile for trainee {string}")
    public void iRequestTheProfileForTrainee(String username) throws Exception {
        lastResult = mockMvc.perform(get("/api/trainees/" + username)
                        .header("Authorization", "Bearer " + currentToken))
                .andReturn();
    }

    @When("I request the profile for trainee {string} without authentication")
    public void iRequestTheProfileForTraineeWithoutAuthentication(String username) throws Exception {
        lastResult = mockMvc.perform(get("/api/trainees/" + username))
                .andReturn();
    }

    @When("I update trainee {string} with:")
    public void iUpdateTraineeWith(String username, DataTable dataTable) throws Exception {
        Map<String, String> data = dataTable.asMap();
        Map<String, Object> request = new HashMap<>();

        // For updates, we need to provide required fields
        Trainee trainee = traineeDAO.findByUsername(username).orElse(null);
        if (trainee != null) {
            request.put("firstName", data.getOrDefault("firstName", trainee.getFirstName()));
            request.put("lastName", data.getOrDefault("lastName", trainee.getLastName()));
            request.put("isActive", trainee.isActive());
        } else {
            request.put("firstName", data.getOrDefault("firstName", ""));
            request.put("lastName", data.getOrDefault("lastName", ""));
            request.put("isActive", true);
        }

        if (data.containsKey("address")) {
            request.put("address", data.get("address"));
        }
        if (data.containsKey("dateOfBirth")) {
            request.put("dateOfBirth", data.get("dateOfBirth"));
        }

        lastResult = mockMvc.perform(put("/api/trainees/" + username)
                        .header("Authorization", "Bearer " + currentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();
    }

    @When("I update trainee {string} with empty required fields")
    public void iUpdateTraineeWithEmptyRequiredFields(String username) throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("firstName", "");
        request.put("lastName", "");
        request.put("isActive", true);

        lastResult = mockMvc.perform(put("/api/trainees/" + username)
                        .header("Authorization", "Bearer " + currentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();
    }

    @When("I deactivate trainee {string}")
    public void iDeactivateTrainee(String username) throws Exception {
        lastResult = mockMvc.perform(patch("/api/trainees/" + username + "/status")
                        .param("isActive", "false")
                        .header("Authorization", "Bearer " + currentToken))
                .andReturn();
    }

    @When("I activate trainee {string}")
    public void iActivateTrainee(String username) throws Exception {
        lastResult = mockMvc.perform(patch("/api/trainees/" + username + "/status")
                        .param("isActive", "true")
                        .header("Authorization", "Bearer " + currentToken))
                .andReturn();
    }

    @Then("the registration should be successful")
    public void theRegistrationShouldBeSuccessful() {
        assertThat(lastResult.getResponse().getStatus()).isEqualTo(201);
    }

    @Then("the registration should fail with status {int}")
    public void theRegistrationShouldFailWithStatus(int status) {
        assertThat(lastResult.getResponse().getStatus()).isEqualTo(status);
    }

    @Then("the response should contain a generated username")
    public void theResponseShouldContainAGeneratedUsername() throws Exception {
        JsonNode response = objectMapper.readTree(lastResult.getResponse().getContentAsString());
        assertThat(response.has("username")).isTrue();
        assertThat(response.get("username").asText()).isNotEmpty();
    }

    @Then("the response should contain a generated password")
    public void theResponseShouldContainAGeneratedPassword() throws Exception {
        JsonNode response = objectMapper.readTree(lastResult.getResponse().getContentAsString());
        assertThat(response.has("password")).isTrue();
        assertThat(response.get("password").asText()).isNotEmpty();
    }

    @Then("the response status should be {int}")
    public void theResponseStatusShouldBe(int status) {
        assertThat(lastResult.getResponse().getStatus()).isEqualTo(status);
    }

    @Then("the profile should contain firstName {string}")
    public void theProfileShouldContainFirstName(String firstName) throws Exception {
        JsonNode response = objectMapper.readTree(lastResult.getResponse().getContentAsString());
        assertThat(response.get("firstName").asText()).isEqualTo(firstName);
    }

    @Then("the profile should contain lastName {string}")
    public void theProfileShouldContainLastName(String lastName) throws Exception {
        JsonNode response = objectMapper.readTree(lastResult.getResponse().getContentAsString());
        assertThat(response.get("lastName").asText()).isEqualTo(lastName);
    }

    @Then("the profile should contain address {string}")
    public void theProfileShouldContainAddress(String address) throws Exception {
        JsonNode response = objectMapper.readTree(lastResult.getResponse().getContentAsString());
        assertThat(response.get("address").asText()).isEqualTo(address);
    }

    @Then("the update should be successful")
    public void theUpdateShouldBeSuccessful() {
        assertThat(lastResult.getResponse().getStatus()).isEqualTo(200);
    }

    @Then("the update should fail with status {int}")
    public void theUpdateShouldFailWithStatus(int status) {
        assertThat(lastResult.getResponse().getStatus()).isEqualTo(status);
    }

    @Then("the trainee {string} should be inactive")
    public void theTraineeShouldBeInactive(String username) {
        Trainee trainee = traineeDAO.findByUsername(username).orElse(null);
        assertThat(trainee).isNotNull();
        assertThat(trainee.isActive()).isFalse();
    }

    @Then("the trainee {string} should be active")
    public void theTraineeShouldBeActive(String username) {
        Trainee trainee = traineeDAO.findByUsername(username).orElse(null);
        assertThat(trainee).isNotNull();
        assertThat(trainee.isActive()).isTrue();
    }
}
