package com.PBL6.Ecommerce.service.chatbot;

import com.PBL6.Ecommerce.domain.dto.chat.IntentClassificationResult;

public interface IntentClassifier {
    IntentClassificationResult classify(String message);
}

