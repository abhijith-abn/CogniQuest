package com.cogniquest.cogniquest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class CogniQuestApplication {
	public static void main(String[] args) {
		SpringApplication.run(CogniQuestApplication.class, args);
	}

	// Define the RestTemplate bean to be used by the application
	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();

	}
}