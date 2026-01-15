package com.cogniquest.cogniquest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper; // <-- CRUCIAL IMPORT
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

@SpringBootApplication
public class CogniQuestApplication {
	public static void main(String[] args) {
		SpringApplication.run(CogniQuestApplication.class, args);
	}

	// 1. RestTemplate Bean (for HTTP calls)
	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	// 2. ObjectMapper Bean (for JSON parsing - FIXES THE INJECTION ERROR)
	@Bean
	public ObjectMapper objectMapper() {
		return new ObjectMapper();
	}

	// 3. CORS Configuration
	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/api/**")
						.allowedOrigins("http://localhost:3000")
						.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
			}
		};
	}
}