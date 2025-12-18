package com.PBL6.Ecommerce.domain.dto.order;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(description = "DTO cho yêu cầu hoàn tiền - hỗ trợ cả MoMo refund và SportyPay refund")
public class RefundRequestDTO {
    
    @Schema(description = "Mã đối tác MoMo (chỉ dùng cho MoMo refund)", example = "MOMO")
    @JsonProperty("partnerCode")
    private String partnerCode;
    
    @Schema(description = "Mã đơn hàng (chỉ dùng cho MoMo refund)", example = "ORDER123456")
    @JsonProperty("orderId")
    private String orderId;
    
    @Schema(description = "Mã yêu cầu refund (chỉ dùng cho MoMo refund)", example = "REF123456")
    @JsonProperty("requestId")
    private String requestId;
    
    @Schema(description = "Số tiền yêu cầu hoàn (VNĐ)", example = "500000", required = true)
    @JsonProperty("amount")
    @NotNull(message = "Số tiền hoàn không được để trống")
    @Positive(message = "Số tiền hoàn phải lớn hơn 0")
    private Long amount;
    
    @Schema(description = "Mã giao dịch MoMo (chỉ dùng cho MoMo refund)", example = "1234567890")
    @JsonProperty("transId")
    private Long transId;
    
    @Schema(description = "Ngôn ngữ", example = "vi", defaultValue = "vi")
    @JsonProperty("lang")
    private String lang = "vi";
    
    @Schema(
        description = "Lý do yêu cầu hoàn tiền (bắt buộc)", 
        example = "Sản phẩm bị lỗi, không hoạt động được",
        required = true,
        minLength = 10,
        maxLength = 1000
    )
    @JsonProperty("description")
    @NotBlank(message = "Lý do hoàn tiền không được để trống")
    @Size(min = 10, max = 1000, message = "Lý do phải từ 10-1000 ký tự")
    private String description;
    
    @Schema(description = "Chữ ký MoMo (chỉ dùng cho MoMo refund)")
    @JsonProperty("signature")
    private String signature;
    
    @Schema(
        description = "URL ảnh bằng chứng từ khách hàng. " +
                      "Có thể là 1 URL đơn hoặc JSON array chứa nhiều URLs: " +
                      "[\"https://cloudinary.com/image1.jpg\", \"https://cloudinary.com/image2.jpg\"]",
        example = "[\"https://res.cloudinary.com/demo/image/upload/sample1.jpg\", \"https://res.cloudinary.com/demo/image/upload/sample2.jpg\"]"
    )
    @JsonProperty("imageUrl")
    @Size(max = 5000, message = "Tổng độ dài URL không được vượt quá 5000 ký tự")
    private String imageUrl;

    // Constructors
    public RefundRequestDTO() {
    }

    public RefundRequestDTO(String partnerCode, String orderId, String requestId, 
                           Long amount, Long transId, String description, String signature) {
        this.partnerCode = partnerCode;
        this.orderId = orderId;
        this.requestId = requestId;
        this.amount = amount;
        this.transId = transId;
        this.description = description;
        this.signature = signature;
    }

    // Getters and Setters
    public String getPartnerCode() {
        return partnerCode;
    }

    public void setPartnerCode(String partnerCode) {
        this.partnerCode = partnerCode;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public Long getTransId() {
        return transId;
    }

    public void setTransId(Long transId) {
        this.transId = transId;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Override
    public String toString() {
        return "RefundRequestDTO{" +
                "partnerCode='" + partnerCode + '\'' +
                ", orderId='" + orderId + '\'' +
                ", requestId='" + requestId + '\'' +
                ", amount=" + amount +
                ", transId=" + transId +
                ", description='" + description + '\'' +
                '}';
    }
}
