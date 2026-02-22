package com.gym.trainer_workload_service.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gym.trainer_workload_service.dto.WorkloadRequest;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class WorkloadMessageConsumer {

    private final ObjectMapper objectMapper;

    public WorkloadMessageConsumer() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @SqsListener("${workload.queue.name}")
    public void receiveMessage(String message) {
        try {
            WorkloadRequest request = objectMapper.readValue(message, WorkloadRequest.class);
            log.info("Received SQS message for trainer: {}, action: {}, duration: {} mins",
                    request.getTrainerUsername(), request.getActionType(), request.getTrainingDuration());
            // TODO: MongoDB processing commented out - will be replaced with DynamoDB in future module
        } catch (JsonProcessingException e) {
            log.error("Failed to process SQS message: {}", e.getMessage());
        }
    }
}
