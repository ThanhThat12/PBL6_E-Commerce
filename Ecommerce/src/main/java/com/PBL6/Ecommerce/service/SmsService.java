package com.PBL6.Ecommerce.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
@Service
public class SmsService {

    @Value("${twilio.accountSid}")
    private String accountSid;

    @Value("${twilio.authToken}")
    private String authToken;

    @Value("${twilio.phoneNumber}")
    private String fromNumber;

    public void sendOtp(String toPhone, String otp) {
        Twilio.init(accountSid, authToken);
        Message.creator(
                new PhoneNumber(toPhone),
                new PhoneNumber(fromNumber),
                "Mã OTP của bạn là: " + otp + " (hiệu lực 5 phút)"
        ).create();
    }
}
