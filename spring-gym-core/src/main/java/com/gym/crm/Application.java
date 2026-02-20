package com.gym.crm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.jms.artemis.ArtemisAutoConfiguration;
import org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration;
// import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
// import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(exclude = {
	DataSourceAutoConfiguration.class,
	HibernateJpaAutoConfiguration.class,
	ArtemisAutoConfiguration.class,
	JmsAutoConfiguration.class
})
// @EnableDiscoveryClient
// @EnableFeignClients
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
