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

    // Gemini API endpoint for content generation
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-pro-latest:generateContent?key=";

    public SyllabusService(SyllabusRepository syllabusRepository, RestTemplate restTemplate) {
        this.syllabusRepository = syllabusRepository;
        this.restTemplate = restTemplate;
    }

    public Syllabus processSyllabus(SyllabusRequest syllabusRequest) {
        String syllabusText = syllabusRequest.getSyllabusText();
        List<Topic> extractedTopics = callGeminiApiForTopics(syllabusText);

        Syllabus syllabus = new Syllabus();
        syllabus.setSyllabusText(syllabusText);
        syllabus.setTopics(extractedTopics);

        return syllabusRepository.save(syllabus);
    }

    private List<Topic> callGeminiApiForTopics(String syllabusText) {
        try {
            // Construct the prompt
            String prompt = String.format(
                    "As an expert educator with extensive experience in curriculum development, your task is to analyze the following syllabus and extract a list of all key topics.\n\n" +
                            "### Instructions:\n" +
                            "- **Analyze** the syllabus and identify up to 30 specific and distinct topics.\n" +
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
            String jsonResponse = restTemplate.postForObject(GEMINI_API_URL + geminiApiKey, entity, String.class);

            // Parse the response
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonResponse);

            List<String> topicNames = new ArrayList<>();
            JsonNode textNode = rootNode.path("candidates").get(0).path("content").path("parts").get(0).path("text");
            if (textNode != null && textNode.isArray()) {
                for (JsonNode nameNode : textNode) {
                    topicNames.add(nameNode.asText());
                }
            } else if (textNode != null && textNode.isTextual()) {
                // Fallback to handle non-array string responses and parse them
                String jsonString = textNode.asText();
                JsonNode arrayNode = objectMapper.readTree(jsonString);
                if (arrayNode.isArray()) {
                    for (JsonNode nameNode : arrayNode) {
                        topicNames.add(nameNode.asText());
                    }
                }
            }

            return topicNames.stream()
                    .map(name -> {
                        Topic topic = new Topic();
                        topic.setName(name);
                        return topic;
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}