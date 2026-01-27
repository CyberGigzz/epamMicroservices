package com.gym.trainer_workload_service.cucumber;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration;
import org.springframework.boot.autoconfigure.jms.artemis.ArtemisAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Cucumber Spring configuration for component tests.
 *
 * Uses Testcontainers to spin up a real MongoDB instance for testing.
 * Disables Eureka and ActiveMQ since we're testing the service in isolation.
 */
@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@EnableAutoConfiguration(exclude = {ArtemisAutoConfiguration.class, JmsAutoConfiguration.class})
@Testcontainers
@ActiveProfiles("test")
public class CucumberSpringConfiguration {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0");

    // Mock JmsTemplate since we're not testing messaging in component tests
    private JmsTemplate jmsTemplate;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        mongoDBContainer.start();
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
        // Disable Eureka for component tests
        registry.add("eureka.client.enabled", () -> "false");
        // Provide dummy values for JMS properties
        registry.add("workload.queue.name", () -> "test-queue");
        registry.add("workload.dlq.name", () -> "test-dlq");
    }
}
