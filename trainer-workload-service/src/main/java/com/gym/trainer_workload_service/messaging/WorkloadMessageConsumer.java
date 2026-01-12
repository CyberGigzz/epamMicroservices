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
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class WorkloadMessageConsumer {

    private final WorkloadService workloadService;
    private final ObjectMapper objectMapper;

    public WorkloadMessageConsumer(WorkloadService workloadService) {
        this.workloadService = workloadService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @JmsListener(destination = "${workload.queue.name}")
    public void receiveMessage(Message message) {
        try {
            // Extract transactionId from message properties
            String transactionId = message.getStringProperty("transactionId");
            if (transactionId != null) {
                MDC.put("transactionId", transactionId);
            }

            // Extract message body
            String jsonBody = ((TextMessage) message).getText();
            log.info("Received message from queue: {}", jsonBody);

            // Deserialize JSON to WorkloadRequest
            WorkloadRequest request = objectMapper.readValue(jsonBody, WorkloadRequest.class);

            // Process the workload
            workloadService.processWorkload(request);

            log.info("Successfully processed workload message for trainer: {}", 
                    request.getTrainerUsername());

        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize message: {}", e.getMessage());
            throw new RuntimeException("Invalid message format", e);
        } catch (Exception e) {
            log.error("Failed to process message: {}", e.getMessage());
            throw new RuntimeException("Message processing failed", e);
        } finally {
            MDC.remove("transactionId");
        }
    }
}
