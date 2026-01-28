package com.gym.trainer_workload_service.integration;

import io.cucumber.spring.CucumberContextConfiguration;
import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.core.server.ActiveMQServer;
import org.apache.activemq.artemis.core.server.ActiveMQServers;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Cucumber Spring configuration for integration tests.
 *
 * Uses:
 * - Testcontainers MongoDB for database
 * - Embedded ActiveMQ Artemis for messaging
 *
 * This tests the full integration: Message -> Consumer -> MongoDB
 */
@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("integration")
public class IntegrationTestConfiguration {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0");

    private static ActiveMQServer activeMQServer;
    private static final int ARTEMIS_PORT = 61617;

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
        mongoDBContainer.start();

        // MongoDB configuration
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);

        // ActiveMQ Artemis configuration
        registry.add("spring.artemis.mode", () -> "native");
        registry.add("spring.artemis.broker-url", () -> "tcp://localhost:" + ARTEMIS_PORT);
        registry.add("spring.artemis.user", () -> "admin");
        registry.add("spring.artemis.password", () -> "admin");

        // Queue names
        registry.add("workload.queue.name", () -> "workload-queue");
        registry.add("workload.dlq.name", () -> "workload-queue-dlq");

        // Disable Eureka for integration tests
        registry.add("eureka.client.enabled", () -> "false");

        // JWT secret for tests
        registry.add("jwt.secret-key", () -> "dGVzdFNlY3JldEtleTEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MA==");
    }
}
