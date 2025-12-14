package com.PBL6.Ecommerce.domain.dto.chat;

import com.PBL6.Ecommerce.constant.IntentType;

public class IntentClassificationResult {
    private final IntentType intent;
    private final double confidence;
    private final boolean fromFallback;

    public IntentClassificationResult(IntentType intent, double confidence, boolean fromFallback) {
        this.intent = intent;
        this.confidence = confidence;
        this.fromFallback = fromFallback;
    }

    public IntentType getIntent() {
        return intent;
    }

    public double getConfidence() {
        return confidence;
    }

    public boolean isFromFallback() {
        return fromFallback;
    }
}

