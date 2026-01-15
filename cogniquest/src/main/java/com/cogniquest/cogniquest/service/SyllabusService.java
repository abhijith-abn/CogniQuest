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
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.NoSuchElementException;

@Service
public class SyllabusService {

    private final SyllabusRepository syllabusRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.model}")
    private String geminiModel;

    private static final String GEMINI_GENERATE_URL = "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s";
    private static final String GEMINI_LIST_MODELS_URL = "https://generativelanguage.googleapis.com/v1beta/models?key=%s";


    public SyllabusService(SyllabusRepository syllabusRepository, RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.syllabusRepository = syllabusRepository;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public List<Syllabus> findAllSyllabi() {
        return syllabusRepository.findAll();
    }

    @Transactional
    public Syllabus finalizeTopics(Long syllabusId, List<Topic> topics) {
        Syllabus syllabus = syllabusRepository.findById(syllabusId)
                .orElseThrow(() -> new NoSuchElementException("Syllabus not found with ID: " + syllabusId));

        // 1. Clear existing topics (JPA handles removal from DB for orphanRemoval=true)
        syllabus.getTopics().clear();

        // 2. Link new/edited topics and add them back
        topics.forEach(t -> t.setSyllabusId(syllabusId));
        syllabus.getTopics().addAll(topics);

        // 3. Set approval status
        syllabus.setIsApproved(true);

        return syllabusRepository.save(syllabus);
    }

    @Transactional
    public Syllabus processSyllabus(SyllabusRequest syllabusRequest) {
        String syllabusText = syllabusRequest.getSyllabusText();
        String courseName = syllabusRequest.getCourseName();

        if (syllabusText == null || syllabusText.trim().isEmpty() || courseName == null || courseName.trim().isEmpty()) {
            throw new IllegalArgumentException("Syllabus text and Course Name cannot be empty.");
        }

        if (!checkApiHealth()) {
            throw new RuntimeException("Gemini API is unavailable.");
        }

        List<Topic> extractedTopics = callGeminiApiForTopics(syllabusText);

        // 1. Create and save the parent Syllabus entity
        Syllabus syllabus = new Syllabus();
        syllabus.setCourseName(courseName);
        syllabus.setIsApproved(false); // Initially not approved
        syllabus.setTeacherId(1L); // Placeholder Teacher ID
        syllabus = syllabusRepository.save(syllabus);

        // 2. Link extracted topics to the Syllabus ID
        Long finalSyllabusId = syllabus.getId();
        extractedTopics.forEach(topic -> topic.setSyllabusId(finalSyllabusId));

        // 3. Set the topics on the syllabus and save again
        syllabus.setTopics(extractedTopics);
        return syllabusRepository.save(syllabus);
    }

    public boolean checkApiHealth() {
        // EXPLICIT CHECK: Ensure the key is not the placeholder text
        if (geminiApiKey == null || geminiApiKey.trim().isEmpty() || geminiApiKey.startsWith("YOUR_GEMINI_API_KEY")) {
            System.err.println("API Key Error: Key is missing or appears to be a placeholder.");
            return false;
        }

        String listModelsUrl = String.format(GEMINI_LIST_MODELS_URL, geminiApiKey);

        try {
            // Attempt a lightweight connection check
            restTemplate.exchange(listModelsUrl, HttpMethod.GET, HttpEntity.EMPTY, String.class);
            System.out.println("Gemini API Health Check: SUCCESS - Key is valid and API is reachable.");
            return true;
        } catch (HttpClientErrorException e) {
            System.err.println("Gemini API Health Check: FAILURE - Status " + e.getStatusCode() + ". Response Body: " + e.getResponseBodyAsString());
            return false;
        } catch (ResourceAccessException e) {
            System.err.println("Gemini API Health Check: FAILURE - Network/SSL error. Check connection or firewall.");
            return false;
        } catch (Exception e) {
            System.err.println("Gemini API Health Check: FAILURE - An unexpected error occurred: " + e.getMessage());
            return false;
        }
    }


    private List<Topic> callGeminiApiForTopics(String syllabusText) {
        String apiUrl = String.format(GEMINI_GENERATE_URL, geminiModel, geminiApiKey);

        try {
            String prompt = String.format(
                    "As an expert educator with extensive experience in curriculum development, your task is to analyze the following syllabus and extract a complete and granular list of all key topics.\n\n" +
                            "### Instructions:\n" +
                            "- **Analyze** the entire syllabus and **extract every specific, foundational concept** that should be assessed.\n" +
                            "- **Identify** up to 30 unique and distinct topics (**aim for maximum coverage**).\n" +
                            "- **Do not** include subtopics or group multiple concepts into one topic.\n" +
                            "- **Strictly organize** the output as a JSON array of strings.\n" +
                            "- **Do not** include any conversational text, explanations, or extraneous characters outside of the JSON array.\n\n" +
                            "Syllabus:\n%s", syllabusText
            );

            Map<String, Object> contents = new HashMap<>();
            Map<String, String> parts = new HashMap<>();
            parts.put("text", prompt);
            contents.put("parts", List.of(parts));

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("contents", List.of(contents));
            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("responseMimeType", "application/json");
            requestBody.put("generationConfig", generationConfig);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            String jsonResponse = restTemplate.postForObject(apiUrl, entity, String.class);

            // --- JSON Parsing Logic ---
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            List<String> topicNames = new ArrayList<>();
            JsonNode textNode = rootNode.path("candidates").get(0).path("content").path("parts").get(0).path("text");

            if (textNode != null && textNode.isTextual()) {
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
            // Specific logging for generation errors
            System.err.println("Gemini GENERATION Error: Status " + e.getStatusCode() + ". Response Body: " + e.getResponseBodyAsString());
            return new ArrayList<>();
        }
        catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}