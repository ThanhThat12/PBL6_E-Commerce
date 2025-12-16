package com.PBL6.Ecommerce.controller.chatbot;

import com.PBL6.Ecommerce.constant.IntentType;
import com.PBL6.Ecommerce.domain.dto.ResponseDTO;
import com.PBL6.Ecommerce.domain.dto.chat.IntentClassificationResult;
import com.PBL6.Ecommerce.service.chatbot.BuyerIntentService;
import com.PBL6.Ecommerce.service.chatbot.CohereTextGenerator;
import com.PBL6.Ecommerce.repository.ProductRepository;
import org.springframework.data.domain.PageRequest;
import com.PBL6.Ecommerce.domain.entity.product.Product;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple REST controller for chatbot Q&A.
 * Endpoint: POST /api/chatbot/ask { question: string }
 * Returns: { answer: string, intent: string, confidence: number }
 */
@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    private final BuyerIntentService intentService;
    private final CohereTextGenerator textGenerator;
    private final ProductRepository productRepository;

    @GetMapping("/ping")
    public ResponseEntity<ResponseDTO<String>> ping() {
        return ResponseDTO.success("OK", "Chatbot OK");
    }

    @PostMapping("/ask")
    public ResponseEntity<ResponseDTO<Map<String, Object>>> ask(@RequestBody AskRequest request) {
        String question = request != null ? request.getQuestion() : null;
        if (question == null || question.trim().isEmpty()) {
            return ResponseDTO.badRequest("Câu hỏi không được để trống");
        }

        IntentClassificationResult result = intentService.classify(question);
        String context = buildContext(result.getIntent(), question);
        String aiAnswer = textGenerator.generateReply(question, context);
        String answer = aiAnswer != null ? aiAnswer : buildAnswer(result.getIntent());

        Map<String, Object> payload = new HashMap<>();
        payload.put("answer", answer);
        payload.put("intent", result.getIntent().name());
        payload.put("confidence", result.getConfidence());
        payload.put("source", result.isFromFallback() ? "rule" : "cohere");
        payload.put("answerSource", aiAnswer != null ? "cohere" : "default");

        return ResponseDTO.success(payload, "Chatbot trả lời thành công");
    }

    private String buildContext(IntentType intent, String question) {
        if (intent == null) intent = IntentType.UNKNOWN;
        switch (intent) {
            case FIND_PRODUCT -> {
                // Lấy gợi ý top sản phẩm theo câu hỏi người dùng
                var products = productRepository.findProductsForSuggestion(
                        question.toLowerCase(), PageRequest.of(0, 5));
                if (products == null || products.isEmpty()) {
                    return "Intent: FIND_PRODUCT\nKhông tìm thấy gợi ý theo câu hỏi.";
                }
                StringBuilder sb = new StringBuilder();
                sb.append("Intent: FIND_PRODUCT\nCác gợi ý sản phẩm (tối đa 5):\n");
                for (Product p : products) {
                    sb.append("- ")
                      .append(p.getName())
                      .append(" | Giá: ")
                      .append(p.getBasePrice())
                      .append(" | Đánh giá: ")
                      .append(p.getRating())
                      .append(" | Đã bán: ")
                      .append(p.getSoldCount())
                      .append("\n");
                }
                return sb.toString();
            }
            case ASK_PRICE -> {
                // Thử tìm theo tên để có dữ liệu giá tham chiếu
                var products = productRepository.findProductsForSuggestion(
                        question.toLowerCase(), PageRequest.of(0, 3));
                StringBuilder sb = new StringBuilder("Intent: ASK_PRICE\n");
                if (products != null && !products.isEmpty()) {
                    sb.append("Một số giá tham khảo:\n");
                    for (Product p : products) {
                        sb.append("- ")
                          .append(p.getName())
                          .append(" | Giá: ")
                          .append(p.getBasePrice())
                          .append("\n");
                    }
                }
                return sb.toString();
            }
            case TRACK_ORDER -> {
                // Có thể mở rộng: parse mã đơn/ghn code và truy vấn ShipmentRepository
                return "Intent: TRACK_ORDER\nNếu cung cấp mã đơn hàng, sẽ tra cứu và trả về tình trạng.";
            }
            case SHIPPING_INFO -> {
                return "Intent: SHIPPING_INFO\nTrả lời về phí/ thời gian giao dựa trên chính sách chung.";
            }
            case RETURN_POLICY -> {
                return "Intent: RETURN_POLICY\nTrả lời chính sách đổi trả/hoàn tiền theo quy định hiện hành.";
            }
            case UNKNOWN -> {
                return "Intent: UNKNOWN\nKhông đủ dữ liệu để suy ra ngữ cảnh.";
            }
        }
        return "";
    }

    private String buildAnswer(IntentType intent) {
        if (intent == null) intent = IntentType.UNKNOWN;
        return switch (intent) {
            case ASK_PRICE -> "Bạn có thể xem giá trực tiếp trên trang sản phẩm. Nếu cần, hãy gửi link sản phẩm để mình hỗ trợ cụ thể.";
            case FIND_PRODUCT -> "Bạn đang tìm sản phẩm gì? Hãy nhập tên, loại hoặc đặc điểm (ví dụ: 'áo sơ mi nam size M').";
            case TRACK_ORDER -> "Bạn vui lòng cung cấp mã đơn hàng để mình tra cứu tình trạng giao hàng.";
            case SHIPPING_INFO -> "Phí và thời gian giao hàng sẽ hiển thị tại bước thanh toán sau khi bạn chọn địa chỉ nhận hàng.";
            case RETURN_POLICY -> "Chính sách đổi trả: hỗ trợ đổi trả trong 7 ngày nếu sản phẩm lỗi. Bạn có thể mở mục 'Đơn hàng' để tạo yêu cầu.";
            case UNKNOWN -> "Xin lỗi, mình chưa hiểu câu hỏi. Bạn có thể diễn đạt lại hoặc cung cấp thêm thông tin không?";
        };
    }

    @Data
    private static class AskRequest {
        @NotBlank
        private String question;
    }
}
