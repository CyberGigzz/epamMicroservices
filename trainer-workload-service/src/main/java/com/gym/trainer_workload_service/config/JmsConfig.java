package com.gym.trainer_workload_service.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.util.ErrorHandler;

@Configuration
@Slf4j
public class JmsConfig {

    @Value("${workload.dlq.name}")
    private String dlqName;

    @Bean
    public ErrorHandler jmsErrorHandler(JmsTemplate jmsTemplate) {
        return throwable -> {
            log.error("JMS Error occurred: {}", throwable.getMessage());
            // The actual DLQ routing is handled in the consumer
        };
    }
}
