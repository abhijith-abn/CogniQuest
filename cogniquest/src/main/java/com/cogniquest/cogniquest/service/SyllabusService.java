// src/main/java/com/cogniquest/service/SyllabusService.java
package com.cogniquest.cogniquest.service;

import com.cogniquest.cogniquest.dto.SyllabusRequest;
import com.cogniquest.cogniquest.model.Syllabus;
import com.cogniquest.cogniquest.model.Topic;
import com.cogniquest.cogniquest.repository.SyllabusRepository;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.http.HttpMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SyllabusService {

    private final SyllabusRepository syllabusRepository;
    private final RestTemplate restTemplate;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    // Injecting the model name from application.properties
    @Value("${gemini.api.model}")
    private String geminiModel;

    // Base API URL for content generation
    private static final String GEMINI_GENERATE_URL = "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s";

    // Endpoint for listing models (used for health check)
    private static final String GEMINI_LIST_MODELS_URL = "https://generativelanguage.googleapis.com/v1beta/models?key=%s";


    public SyllabusService(SyllabusRepository syllabusRepository, RestTemplate restTemplate) {
        this.syllabusRepository = syllabusRepository;
        this.restTemplate = restTemplate;
    }

    /**
     * Performs a lightweight check to ensure the Gemini API key and connection are valid.
     * @return true if the API is reachable and the key is accepted, false otherwise.
     */
    public boolean checkApiHealth() {
        // NOTE: Changed placeholder check to match new properties file content
        if (geminiApiKey == null || geminiApiKey.trim().isEmpty() || geminiApiKey.equals("YOUR_GEMINI_API_KEY_HERE")) {
            System.err.println("API Key is missing or placeholder. Cannot check health.");
            return false;
        }

        String listModelsUrl = String.format(GEMINI_LIST_MODELS_URL, geminiApiKey);

        try {
            // Attempt a GET request to the ListModels endpoint
            restTemplate.exchange(listModelsUrl, HttpMethod.GET, HttpEntity.EMPTY, String.class);
            System.out.println("Gemini API Health Check: SUCCESS - Key is valid and API is reachable.");
            return true;
        } catch (HttpClientErrorException e) {
            // Handle 4xx errors (e.g., 401 Unauthorized, 403 Forbidden, 429 Quota Exceeded)
            System.err.println("Gemini API Health Check: FAILURE - API returned status " + e.getStatusCode() + ". Check Key/Quotas.");
            return false;
        } catch (ResourceAccessException e) {
            // Handle network errors (e.g., connection refused, timeout, SSL issue)
            System.err.println("Gemini API Health Check: FAILURE - Network/SSL error. Check connection or firewall.");
            return false;
        } catch (Exception e) {
            System.err.println("Gemini API Health Check: FAILURE - An unexpected error occurred: " + e.getMessage());
            return false;
        }
    }

    public Syllabus processSyllabus(SyllabusRequest syllabusRequest) {
        String syllabusText = syllabusRequest.getSyllabusText();

        // Input validation and health check
        if (syllabusText == null || syllabusText.trim().isEmpty()) {
            System.err.println("Syllabus text is empty. Cannot call Gemini API.");
            return new Syllabus();
        }

        if (!checkApiHealth()) {
            System.err.println("Skipping syllabus processing due to failed API health check.");
            // Returning an empty syllabus object allows the frontend to show an empty state or error message.
            Syllabus failedSyllabus = new Syllabus();
            failedSyllabus.setSyllabusText(syllabusText);
            return failedSyllabus;
        }

        List<Topic> extractedTopics = callGeminiApiForTopics(syllabusText);

        Syllabus syllabus = new Syllabus();
        syllabus.setSyllabusText(syllabusText);
        syllabus.setTopics(extractedTopics);

        return syllabusRepository.save(syllabus);
    }

    private List<Topic> callGeminiApiForTopics(String syllabusText) {
        // Construct the full API URL using the corrected model name and key
        String apiUrl = String.format(GEMINI_GENERATE_URL, geminiModel, geminiApiKey);

        try {
            // Construct the prompt with formatting instructions
            String prompt = String.format(
                    "As an expert educator with extensive experience in curriculum development, your task is to analyze the following syllabus and extract a complete and granular list of all key topics.\n\n" +
                            "### Instructions:\n" +
                            "- **Analyze** the entire syllabus and **extract every specific, foundational concept** that should be assessed.\n" +
                            "- **Identify** up to 30 unique and distinct topics (aim for maximum coverage).\n" + // Changed instruction to emphasize "maximum coverage"
                            "- **Do not** include subtopics or group multiple concepts into one topic.\n" +
                            "- **Strictly organize** the output as a JSON array of strings.\n" +
                            "- **Do not** include any conversational text, explanations, or extraneous characters outside of the JSON array.\n\n" +
                            "Syllabus:\n%s", syllabusText
            );

            // Create the API request body
            Map<String, Object> contents = new HashMap<>();
            Map<String, String> parts = new HashMap<>();
            parts.put("text", prompt);
            contents.put("parts", List.of(parts));

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("contents", List.of(contents));
            requestBody.put("generationConfig", Map.of(
                    "responseMimeType", "application/json"
            ));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // Make the API call
            String jsonResponse = restTemplate.postForObject(apiUrl, entity, String.class);

            // --- JSON Parsing Logic ---
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonResponse);

            List<String> topicNames = new ArrayList<>();
            JsonNode textNode = rootNode.path("candidates").get(0).path("content").path("parts").get(0).path("text");

            if (textNode != null && textNode.isTextual()) {
                // The API guarantees a JSON output, but we need to parse the text content
                try {
                    JsonNode arrayNode = objectMapper.readTree(textNode.asText());
                    if (arrayNode.isArray()) {
                        for (JsonNode nameNode : arrayNode) {
                            topicNames.add(nameNode.asText());
                        }
                    }
                } catch (Exception innerE) {
                    System.err.println("Could not parse JSON array from Gemini response text: " + textNode.asText());
                }
            }

            return topicNames.stream()
                    .map(name -> {
                        Topic topic = new Topic();
                        topic.setName(name);
                        return topic;
                    })
                    .collect(Collectors.toList());

        } catch (HttpClientErrorException e) {
            // Log specific 4xx and 5xx API errors (e.g., 429 Quota Exceeded)
            System.err.println("Gemini API Error: Status " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            return new ArrayList<>();
        }
        catch (Exception e) {
            // Log other exceptions (network, JSON mapping, etc.)
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}