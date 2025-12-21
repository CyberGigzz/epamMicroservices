package com.gym.crm.client;

import com.gym.crm.dto.WorkloadRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "trainer-workload-service")
public interface TrainerWorkloadClient {

    @PostMapping("/api/trainers/workload")
    void updateWorkload(
            @RequestHeader("Authorization") String authToken,
            @RequestHeader(value = "X-Transaction-Id", required = false) String transactionId,
            @RequestBody WorkloadRequest request
    );
}
