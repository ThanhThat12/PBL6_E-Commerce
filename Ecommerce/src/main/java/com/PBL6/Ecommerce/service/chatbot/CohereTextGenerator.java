package com.PBL6.Ecommerce.service.chatbot;

import com.PBL6.Ecommerce.config.CohereConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple text generation via Cohere Generate API.
 * Uses a base prompt from configuration, and injects user question and optional context.
 */
@Service
public class CohereTextGenerator {

    private static final Logger logger = LoggerFactory.getLogger(CohereTextGenerator.class);

    private final RestTemplate restTemplate;
    private final CohereConfig cohereConfig;

    @Value("${cohere.api.key}")
    private String apiKey;

    @Value("${cohere.chat.model:command-a-03-2025}")
    private String model;

    @Value("${cohere.chatbot.prompt:Bạn là trợ lý mua sắm thân thiện của một sàn thương mại điện tử Việt Nam. Trả lời ngắn gọn, tự nhiên, ưu tiên tiếng Việt, hướng người dùng tới hành động cụ thể. Nếu có Context kèm theo thì dùng để trả lời chính xác. Nếu không đủ dữ liệu, hãy giải thích cần thêm thông tin.}")
    private String basePrompt;

    public CohereTextGenerator(@Qualifier("cohereRestTemplate") RestTemplate restTemplate,
                               CohereConfig cohereConfig) {
        this.restTemplate = restTemplate;
        this.cohereConfig = cohereConfig;
    }

    /**
     * Generate a natural reply using Cohere's Chat v2 endpoint.
     * Returns null if the call fails.
     */
    public String generateReply(String question, String context) {
        try {
            String url = cohereConfig.getBaseUrl() + "/v2/chat";
            logger.debug("Calling Cohere chat v2: model={} url={}", model, url);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);
            headers.set("Accept", "application/json");

            Map<String, Object> payload = new HashMap<>();
            payload.put("model", model);
            payload.put("messages", buildMessages(question, context));
            payload.put("max_tokens", 256);
            payload.put("temperature", 0.3);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);
            logger.debug("Cohere chat status: {}", response.getStatusCode());

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Object messageObj = response.getBody().get("message");
                if (messageObj instanceof Map<?, ?> msg) {
                    Object contentObj = msg.get("content");
                    if (contentObj instanceof java.util.List<?> contentList && !contentList.isEmpty()) {
                        Object first = contentList.get(0);
                        if (first instanceof Map<?, ?> item) {
                            Object text = item.get("text");
                            if (text instanceof String s && !s.isBlank()) {
                                return s.trim();
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            logger.warn("Cohere chat error: {}", ex.getMessage());
        }
        return null;
    }

    private java.util.List<Map<String, Object>> buildMessages(String question, String context) {
        java.util.List<Map<String, Object>> messages = new java.util.ArrayList<>();

        Map<String, Object> system = new HashMap<>();
        system.put("role", "system");
        system.put("content", basePrompt);
        messages.add(system);

        StringBuilder userContent = new StringBuilder();
        if (context != null && !context.isBlank()) {
            userContent.append("Context:\n").append(context).append("\n\n");
        }
        userContent.append(question);

        Map<String, Object> user = new HashMap<>();
        user.put("role", "user");
        user.put("content", userContent.toString());
        messages.add(user);

        return messages;
    }
}
