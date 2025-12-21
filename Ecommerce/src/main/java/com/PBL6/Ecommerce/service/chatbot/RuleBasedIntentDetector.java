package com.PBL6.Ecommerce.service.chatbot;

import com.PBL6.Ecommerce.constant.IntentType;
import com.PBL6.Ecommerce.domain.dto.chat.IntentClassificationResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class RuleBasedIntentDetector implements IntentClassifier {

    private static final Pattern ASK_PRICE = Pattern.compile(
            "(?i)(giá(?:\s*sản\s*phẩm)?|giá\s*tiền|bao\s*nhiêu|chi\s*phí|đắt\s*rẻ)"
    );

    private static final Pattern FIND_PRODUCT = Pattern.compile(
            "(?i)(tìm\s*kiếm|tìm|có\s+.+\s+không|mặt\s*hàng|sản\s*phẩm|mẫu|loại)"
    );

    private static final Pattern TRACK_ORDER = Pattern.compile(
            "(?i)(đơn\s*hàng|mã\s*đơn|theo\s*dõi|tra\s*cứu|đang\s*ở\s*đâu|tình\s*trạng\s*giao)"
    );

    private static final Pattern SHIPPING_INFO = Pattern.compile(
            "(?i)(vận\s*chuyển|giao\s*hàng|phí\s*ship|phí\s*vận\s*chuyển|thời\s*gian\s*giao|ship)"
    );

    private static final Pattern RETURN_POLICY = Pattern.compile(
            "(?i)(đổi\s*trả|trả\s*hàng|hoàn\s*trả|bảo\s*hành|chính\s*sách\s*đổi\s*trả)"
    );

    @Override
    public IntentClassificationResult classify(String message) {
        if (message == null || message.isBlank()) {
            return new IntentClassificationResult(IntentType.UNKNOWN, 0.0, true);
        }
        String m = message.toLowerCase(Locale.ROOT).trim();

        if (ASK_PRICE.matcher(m).find()) {
            return new IntentClassificationResult(IntentType.ASK_PRICE, 0.85, true);
        }
        if (TRACK_ORDER.matcher(m).find()) {
            return new IntentClassificationResult(IntentType.TRACK_ORDER, 0.85, true);
        }
        if (SHIPPING_INFO.matcher(m).find()) {
            return new IntentClassificationResult(IntentType.SHIPPING_INFO, 0.80, true);
        }
        if (RETURN_POLICY.matcher(m).find()) {
            return new IntentClassificationResult(IntentType.RETURN_POLICY, 0.80, true);
        }
        if (FIND_PRODUCT.matcher(m).find()) {
            return new IntentClassificationResult(IntentType.FIND_PRODUCT, 0.75, true);
        }
        return new IntentClassificationResult(IntentType.UNKNOWN, 0.4, true);
    }
}

