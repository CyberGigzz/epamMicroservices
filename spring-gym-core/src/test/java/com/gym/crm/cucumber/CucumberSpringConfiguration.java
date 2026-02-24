package com.gym.crm.cucumber;

import io.awspring.cloud.sqs.operations.SqsTemplate;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import com.gym.crm.client.TrainerWorkloadClient;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class CucumberSpringConfiguration {

    @MockitoBean
    private SqsTemplate sqsTemplate;

    @MockitoBean
    private TrainerWorkloadClient trainerWorkloadClient;
}
