package com.gym.trainer_workload_service.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gym.trainer_workload_service.dto.WorkloadRequest;
import com.gym.trainer_workload_service.service.WorkloadService;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

// @Component
@Slf4j
public class WorkloadMessageConsumer {

    private final WorkloadService workloadService;
    private final JmsTemplate jmsTemplate;
    private final ObjectMapper objectMapper;

    @Value("${workload.dlq.name}")
    private String dlqName;

    public WorkloadMessageConsumer(WorkloadService workloadService, JmsTemplate jmsTemplate) {
        this.workloadService = workloadService;
        this.jmsTemplate = jmsTemplate;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @JmsListener(destination = "${workload.queue.name}")
    public void receiveMessage(Message message) {
        String jsonBody = null;
        try {
            // Extract transactionId from message properties
            String transactionId = message.getStringProperty("transactionId");
            if (transactionId != null) {
                MDC.put("transactionId", transactionId);
            }

            // Extract message body
            jsonBody = ((TextMessage) message).getText();
            log.info("Received message from queue: {}", jsonBody);

            // Deserialize JSON to WorkloadRequest
            WorkloadRequest request = objectMapper.readValue(jsonBody, WorkloadRequest.class);

            // Process the workload
            workloadService.processWorkload(request);

            log.info("Successfully processed workload message for trainer: {}",
                    request.getTrainerUsername());

        } catch (Exception e) {
            log.error("Failed to process message: {}. Sending to DLQ.", e.getMessage());
            sendToDlq(jsonBody, e);
        } finally {
            MDC.remove("transactionId");
        }
    }

    private void sendToDlq(String messageBody, Exception exception) {
        try {
            jmsTemplate.convertAndSend(dlqName, messageBody, msg -> {
                msg.setStringProperty("errorMessage", exception.getMessage());
                msg.setStringProperty("errorType", exception.getClass().getSimpleName());
                return msg;
            });
            log.info("Message sent to DLQ: {}", dlqName);
        } catch (Exception e) {
            log.error("Failed to send message to DLQ: {}", e.getMessage());
        }
    }
}
