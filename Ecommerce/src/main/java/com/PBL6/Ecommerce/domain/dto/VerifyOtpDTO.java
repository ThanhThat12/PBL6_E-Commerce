package com.PBL6.Ecommerce.domain.dto;

public class VerifyOtpDTO {
    private String contact;
    private String otp;

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }
}