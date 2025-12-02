package com.PBL6.Ecommerce.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for reporting a review
 */
public class ReportReviewRequestDTO {
    
    @NotBlank(message = "Loại báo cáo không được để trống")
    private String reportType;
    
    @NotBlank(message = "Lý do báo cáo không được để trống")
    @Size(min = 10, max = 500, message = "Lý do phải từ 10-500 ký tự")
    private String reason;
    
    // Constructors
    public ReportReviewRequestDTO() {}
    
    public ReportReviewRequestDTO(String reportType, String reason) {
        this.reportType = reportType;
        this.reason = reason;
    }
    
    // Getters and Setters
    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
