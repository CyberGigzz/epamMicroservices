package com.gym.trainer_workload_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.jms.artemis.ArtemisAutoConfiguration;
import org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration;

@SpringBootApplication(exclude = {
	MongoAutoConfiguration.class,
	MongoDataAutoConfiguration.class,
	MongoRepositoriesAutoConfiguration.class,
	ArtemisAutoConfiguration.class,
	JmsAutoConfiguration.class
})
public class TrainerWorkloadServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(TrainerWorkloadServiceApplication.class, args);
	}

}
