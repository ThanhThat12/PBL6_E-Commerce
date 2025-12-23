package com.PBL6.Ecommerce.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Send OTP email to user
     * @param toEmail Recipient email address
     * @param otp OTP code to send
     * @throws RuntimeException if email sending fails
     */
    public void sendOtp(String toEmail, String otp) {
        try {
            log.debug("Attempting to send OTP email to: {}", toEmail);
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Mã OTP xác thực");
            message.setText("Mã OTP của bạn là: " + otp + "\nCó hiệu lực trong 5 phút.");
            
            mailSender.send(message);
            log.info("OTP email sent successfully to: {}", toEmail);
            
        } catch (MailException e) {
            log.error("Failed to send OTP email to: {}. Error: {}", toEmail, e.getMessage(), e);
            throw new RuntimeException("Không thể gửi email OTP. Vui lòng thử lại sau hoặc liên hệ hỗ trợ.", e);
        } catch (Exception e) {
            log.error("Unexpected error while sending OTP email to: {}. Error: {}", toEmail, e.getMessage(), e);
            throw new RuntimeException("Đã xảy ra lỗi khi gửi email. Vui lòng thử lại sau.", e);
        }
    }
}

