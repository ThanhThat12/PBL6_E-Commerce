package com.PBL6.Ecommerce.service.chatbot;

import com.PBL6.Ecommerce.domain.dto.chat.IntentClassificationResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class BuyerIntentService {

    private final CohereIntentClassifier cohere;
    private final RuleBasedIntentDetector rule;
    private static final Logger logger = LoggerFactory.getLogger(BuyerIntentService.class);

    @Value("${cohere.classify.threshold:0.7}")
    private double threshold;

    public BuyerIntentService(CohereIntentClassifier cohere, RuleBasedIntentDetector rule) {
        this.cohere = cohere;
        this.rule = rule;
    }

    public IntentClassificationResult classify(String message) {
        IntentClassificationResult ai = cohere.classify(message);
        if (ai.getConfidence() < threshold) {
            IntentClassificationResult rb = rule.classify(message);
            logger.debug("Using rule-based fallback: intent={} confidence={}", rb.getIntent(), rb.getConfidence());
            return rb;
        }
        logger.debug("Using Cohere result: intent={} confidence={}", ai.getIntent(), ai.getConfidence());
        return ai;
    }
}
