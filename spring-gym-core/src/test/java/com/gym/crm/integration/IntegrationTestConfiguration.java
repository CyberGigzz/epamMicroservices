package com.gym.crm.integration;

import com.gym.crm.client.TrainerWorkloadClient;
import io.cucumber.spring.CucumberContextConfiguration;
import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.core.server.ActiveMQServer;
import org.apache.activemq.artemis.core.server.ActiveMQServers;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Cucumber Spring configuration for integration tests.
 *
 * Uses embedded ActiveMQ Artemis for real messaging.
 * Tests that messages are correctly sent to the queue when trainings are created.
 */
@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("integration")
public class IntegrationTestConfiguration {

    private static ActiveMQServer activeMQServer;
    private static final int ARTEMIS_PORT = 61618;

    // Mock Feign client since we're only testing messaging
    @MockitoBean
    private TrainerWorkloadClient trainerWorkloadClient;

    static {
        try {
            // Start embedded ActiveMQ Artemis
            var config = new ConfigurationImpl();
            config.addAcceptorConfiguration("in-vm", "vm://0");
            config.addAcceptorConfiguration("tcp", "tcp://localhost:" + ARTEMIS_PORT);
            config.setSecurityEnabled(false);
            config.setPersistenceEnabled(false);

            activeMQServer = ActiveMQServers.newActiveMQServer(config);
            activeMQServer.start();
        } catch (Exception e) {
            throw new RuntimeException("Failed to start embedded ActiveMQ Artemis", e);
        }
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        // ActiveMQ Artemis configuration
        registry.add("spring.artemis.mode", () -> "native");
        registry.add("spring.artemis.broker-url", () -> "tcp://localhost:" + ARTEMIS_PORT);
        registry.add("spring.artemis.user", () -> "admin");
        registry.add("spring.artemis.password", () -> "admin");

        // Queue name
        registry.add("workload.queue.name", () -> "workload-queue");

        // Disable Eureka for integration tests
        registry.add("eureka.client.enabled", () -> "false");

        // JWT secret for tests
        registry.add("jwt.secret-key", () -> "dGVzdFNlY3JldEtleTEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MA==");
    }
}
