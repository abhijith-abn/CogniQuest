package com.cogniquest.cogniquest.service;

import com.cogniquest.cogniquest.model.Syllabus;
import com.cogniquest.cogniquest.model.Question;
import com.cogniquest.cogniquest.model.Topic;
import com.cogniquest.cogniquest.model.Difficulty;
import com.cogniquest.cogniquest.repository.SyllabusRepository;
import com.cogniquest.cogniquest.repository.QuestionRepository;
import com.cogniquest.cogniquest.repository.TopicRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class QuestionService {

    private final SyllabusRepository syllabusRepository;
    private final QuestionRepository questionRepository;
    private final TopicRepository topicRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.model}")
    private String geminiModel;

    private static final String GEMINI_GENERATE_URL = "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s";

    public QuestionService(SyllabusRepository syllabusRepository, QuestionRepository questionRepository, TopicRepository topicRepository, RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.syllabusRepository = syllabusRepository;
        this.questionRepository = questionRepository;
        this.topicRepository = topicRepository;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Executes AI generation for a single topic and returns the questions for REVIEW.
     * DOES NOT SAVE TO DB YET. (Used by POST /api/question/generate)
     */
    public List<Question> generateQuestionsForReview(Long topicId) { // NOTE: NO @Transactional
        // 1. Fetch the Topic (which brings the parent Syllabus/Course info)
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new NoSuchElementException("Topic not found with ID: " + topicId));

        Syllabus syllabus = syllabusRepository.findById(topic.getSyllabusId())
                .orElseThrow(() -> new NoSuchElementException("Syllabus not found for topic: " + topicId));

        if (!syllabus.getIsApproved()) {
            throw new IllegalStateException("Topics for this syllabus have not been approved yet. Please finalize the topics first.");
        }

        List<Question> allGeneratedQuestions = new ArrayList<>();

        // 2. Generate questions for all three difficulty levels (EASY, INTERMEDIATE, ADVANCED)
        for (Difficulty difficulty : Difficulty.values()) {
            List<Question> generatedBatch = callGeminiApiForQuestions(
                    topic.getName(),
                    syllabus.getCourseName(),
                    topic.getId(), // Pass topic ID to be set in the question model
                    difficulty
            );
            allGeneratedQuestions.addAll(generatedBatch);
        }

        return allGeneratedQuestions;
    }

    /**
     * Saves the reviewed questions permanently to the database (Persistence step).
     * (Used by PUT /api/question/confirm-and-save/{topicId})
     */
    @Transactional
    public List<Question> confirmAndSaveQuestions(Long topicId, List<Question> reviewedQuestions) {
        // 1. Clean up old questions for this topic to prevent duplicates
        // Note: findByTopicId must be implemented in QuestionRepository
        questionRepository.deleteAll(questionRepository.findByTopicId(topicId));

        // 2. Save the new, reviewed questions permanently
        return questionRepository.saveAll(reviewedQuestions);
    }


    private List<Question> callGeminiApiForQuestions(String topicName, String courseName, Long topicId, Difficulty difficulty) {
        String apiUrl = String.format(GEMINI_GENERATE_URL, geminiModel, geminiApiKey);

        String prompt = buildQuestionPrompt(topicName, courseName, difficulty);
        // JSON Schema to enforce structured output
        String jsonSchema = """
        {
          "type": "ARRAY",
          "items": {
            "type": "OBJECT",
            "properties": {
              "questionText": { "type": "STRING", "description": "The question content." },
              "optionsJson": { "type": "STRING", "description": "A JSON array of 4 options, e.g., ['A. Option 1', 'B. Option 2', ...]." },
              "correctAnswerText": { "type": "STRING", "description": "The exact text of the correct option (e.g., 'Option 3')." }
            }
          }
        }
        """;

        try {
            Map<String, Object> contents = new HashMap<>();
            contents.put("parts", List.of(Map.of("text", prompt)));

            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("responseMimeType", "application/json");
            generationConfig.put("responseSchema", objectMapper.readTree(jsonSchema));

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("contents", List.of(contents));
            requestBody.put("generationConfig", generationConfig);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // Make the API call
            String jsonResponse = restTemplate.postForObject(apiUrl, entity, String.class);

            // 2. Parsing the structured response
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            JsonNode textNode = rootNode.path("candidates").get(0).path("content").path("parts").get(0).path("text");

            if (textNode != null && textNode.isTextual()) {
                JsonNode questionsArray = objectMapper.readTree(textNode.asText());

                List<Question> questions = new ArrayList<>();
                for (JsonNode qNode : questionsArray) {
                    Question q = new Question();
                    q.setQuestionText(qNode.path("questionText").asText());
                    q.setOptionsJson(qNode.path("optionsJson").asText());
                    q.setCorrectAnswerText(qNode.path("correctAnswerText").asText());
                    q.setTopicId(topicId); // Link to the topic
                    q.setDifficulty(Difficulty.valueOf(difficulty.name())); // Set difficulty based on the loop
                    questions.add(q);
                }
                return questions;
            }
            return Collections.emptyList();

        } catch (HttpClientErrorException e) {
            System.err.println("Gemini API Error [" + difficulty + "]: Status " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            return Collections.emptyList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * Builds the AI prompt with clear instructions for difficulty.
     */
    private String buildQuestionPrompt(String topic, String course, Difficulty difficulty) {
        String difficultyInstruction;

        // This logic enforces the difficulty constraints (Easy: Recall, Advanced: Analysis)
        switch (difficulty) {
            case EASY:
                difficultyInstruction = "Create 10 questions focusing on simple definitions, syntax recall, and basic knowledge verification. Use very straightforward language.";
                break;
            case INTERMEDIATE:
                difficultyInstruction = "Create 10 questions requiring application of concepts, moderate problem-solving, and understanding of standard coding practices. Use technical terms accurately.";
                break;
            case ADVANCED:
                difficultyInstruction = "Create 10 questions demanding critical thinking, debugging skills, analysis of complex code scenarios (e.g., pointers/recursion), and synthesis of multiple concepts.";
                break;
            default:
                difficultyInstruction = "Create 10 questions based on core concepts.";
        }

        return String.format(
                "You are an expert educator creating a multiple-choice question bank for the course '%s'.\n\n" +
                        "### Assessment Task:\n" +
                        "Generate **10 unique multiple-choice questions** on the specific topic: **%s**.\n" +
                        "**Difficulty Level:** %s\n\n" +
                        "### Instructions:\n" +
                        "1. %s\n" +
                        "2. Ensure each question has exactly 4 options. Options must be plausible but only one must be correct.\n" +
                        "3. Return the output as a clean JSON array object conforming to the required schema, containing ONLY the `questionText`, `optionsJson` (as a JSON string), and the exact `correctAnswerText`.",
                course, topic, difficulty.name(), difficultyInstruction
        );
    }
}
