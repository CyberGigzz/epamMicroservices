package com.gym.crm.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gym.crm.dto.WorkloadRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
public class WorkloadMessageProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkloadMessageProducer.class);

    private final JmsTemplate jmsTemplate;
    private final ObjectMapper objectMapper;

    @Value("${workload.queue.name}")
    private String queueName;

    public WorkloadMessageProducer(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public void sendWorkloadMessage(WorkloadRequest request) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(request);
            String transactionId = MDC.get("transactionId");

            jmsTemplate.convertAndSend(queueName, jsonMessage, message -> {
                if (transactionId != null) {
                    message.setStringProperty("transactionId", transactionId);
                }
                return message;
            });

            LOGGER.info("Sent workload message to queue '{}': {} {} minutes for trainer {}",
                    queueName,
                    request.getActionType(),
                    request.getTrainingDuration(),
                    request.getTrainerUsername());

        } catch (JsonProcessingException e) {
            LOGGER.error("Failed to serialize workload request: {}", e.getMessage());
            throw new RuntimeException("Failed to send workload message", e);
        }
    }
}
