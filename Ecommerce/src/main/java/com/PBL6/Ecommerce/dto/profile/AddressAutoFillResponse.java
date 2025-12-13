package com.PBL6.Ecommerce.dto.profile;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response DTO for auto-fill address contact info
 * Used for GET /api/addresses/auto-fill
 */
@Schema(description = "Auto-fill contact information from user profile")
public class AddressAutoFillResponse {
    
    @Schema(description = "Contact name from user profile", example = "John Doe")
    private String contactName;
    
    @Schema(description = "Contact phone from user profile", example = "0901234567")
    private String contactPhone;

    public AddressAutoFillResponse() {}

    public AddressAutoFillResponse(String contactName, String contactPhone) {
        this.contactName = contactName;
        this.contactPhone = contactPhone;
    }

    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
}
