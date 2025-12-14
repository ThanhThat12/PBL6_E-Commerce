package com.PBL6.Ecommerce.service.chatbot;

import com.PBL6.Ecommerce.config.CohereConfig;
import com.PBL6.Ecommerce.constant.IntentType;
import com.PBL6.Ecommerce.domain.dto.chat.IntentClassificationResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Service
public class CohereIntentClassifier implements IntentClassifier {

    private final RestTemplate restTemplate;
    private final CohereConfig cohereConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(CohereIntentClassifier.class);

    @Value("${cohere.api.key}")
    private String apiKey;

    public CohereIntentClassifier(@Qualifier("cohereRestTemplate") RestTemplate restTemplate,
                                  CohereConfig cohereConfig) {
        this.restTemplate = restTemplate;
        this.cohereConfig = cohereConfig;
    }

    @Override
    public IntentClassificationResult classify(String message) {
        try {
            String url = cohereConfig.getBaseUrl() + "/classify";
            logger.debug("Calling Cohere classify: url={} message='{}'", url, message);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);
            headers.set("Cohere-Version", "2021-11-08");

            Map<String, Object> payload = new HashMap<>();
            payload.put("inputs", Collections.singletonList(message));
            payload.put("examples", buildExamples());
            payload.put("truncate", "END");

            HttpEntity<String> request = new HttpEntity<>(objectMapper.writeValueAsString(payload), headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
            logger.debug("Cohere response status: {}", response.getStatusCode());

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode classifications = root.path("classifications");
                if (classifications.isArray() && classifications.size() > 0) {
                    JsonNode c = classifications.get(0);
                    String prediction = c.path("prediction").asText(null);
                    double confidence = c.path("confidence").asDouble(0.0);
                    IntentType intent = mapLabel(prediction);
                    if (intent == null) intent = IntentType.UNKNOWN;
                    logger.debug("Cohere classify success: intent={} confidence={} fromFallback=false", intent, confidence);
                    return new IntentClassificationResult(intent, confidence, false);
                }
            }
        } catch (Exception ex) {
            logger.warn("Cohere classify error: {}", ex.getMessage());
            // Swallow error and return unknown with zero confidence; composite service will fallback
        }
        return new IntentClassificationResult(IntentType.UNKNOWN, 0.0, false);
    }

    private List<Map<String, String>> buildExamples() {
        List<Map<String, String>> examples = new ArrayList<>();

        // ASK_PRICE
        examples.add(example("Giá sản phẩm này bao nhiêu?", "ASK_PRICE"));
        examples.add(example("Cho tôi biết giá tiền của mặt hàng này", "ASK_PRICE"));
        examples.add(example("Chi phí là bao nhiêu", "ASK_PRICE"));

        // FIND_PRODUCT
        examples.add(example("Tôi muốn tìm áo sơ mi nam", "FIND_PRODUCT"));
        examples.add(example("Có iPhone 13 không?", "FIND_PRODUCT"));
        examples.add(example("Tìm giày thể thao size 42", "FIND_PRODUCT"));

        // TRACK_ORDER
        examples.add(example("Tra cứu đơn hàng 123456", "TRACK_ORDER"));
        examples.add(example("Đơn hàng của tôi đang ở đâu?", "TRACK_ORDER"));
        examples.add(example("Theo dõi tình trạng giao của đơn", "TRACK_ORDER"));

        // SHIPPING_INFO
        examples.add(example("Phí vận chuyển là bao nhiêu?", "SHIPPING_INFO"));
        examples.add(example("Thời gian giao hàng dự kiến?", "SHIPPING_INFO"));
        examples.add(example("Chính sách giao/ship hàng thế nào", "SHIPPING_INFO"));

        // RETURN_POLICY
        examples.add(example("Chính sách đổi trả của shop", "RETURN_POLICY"));
        examples.add(example("Tôi muốn trả hàng/hoàn trả", "RETURN_POLICY"));
        examples.add(example("Bảo hành và đổi trả ra sao?", "RETURN_POLICY"));

        return examples;
    }

    private Map<String, String> example(String text, String label) {
        Map<String, String> e = new HashMap<>();
        e.put("text", text);
        e.put("label", label);
        return e;
    }

    private IntentType mapLabel(String label) {
        if (label == null) return null;
        try {
            return IntentType.valueOf(label);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
