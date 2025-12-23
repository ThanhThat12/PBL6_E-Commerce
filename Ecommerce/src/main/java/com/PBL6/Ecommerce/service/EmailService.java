package com.PBL6.Ecommerce.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.MailAuthenticationException;
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
            log.info("Attempting to send OTP email to: {}", toEmail);
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Mã OTP xác thực");
            message.setText("Mã OTP của bạn là: " + otp + "\nCó hiệu lực trong 5 phút.");
            
            mailSender.send(message);
            log.info("✅ OTP email sent successfully to: {}", toEmail);
            
        } catch (MailAuthenticationException e) {
            log.error("❌ Authentication failed when sending OTP email to: {}", toEmail);
            log.error("Error details: {}", e.getMessage());
            if (e.getCause() != null) {
                log.error("Root cause: {}", e.getCause().getMessage());
            }
            throw new RuntimeException("Lỗi xác thực email. Vui lòng kiểm tra cấu hình email hoặc liên hệ hỗ trợ.", e);
        } catch (MailSendException e) {
            log.error("❌ Failed to send OTP email to: {}", toEmail);
            log.error("Error details: {}", e.getMessage());
            if (e.getCause() != null) {
                log.error("Root cause: {}", e.getCause().getMessage());
            }
            // Log failed messages if available
            if (e.getFailedMessages() != null && !e.getFailedMessages().isEmpty()) {
                log.error("Failed messages count: {}", e.getFailedMessages().size());
            }
            throw new RuntimeException("Không thể gửi email OTP. Vui lòng kiểm tra kết nối mạng hoặc thử lại sau.", e);
        } catch (MailException e) {
            log.error("❌ Mail exception when sending OTP email to: {}", toEmail);
            log.error("Error type: {}", e.getClass().getSimpleName());
            log.error("Error message: {}", e.getMessage());
            if (e.getCause() != null) {
                log.error("Root cause: {}", e.getCause().getMessage());
            }
            throw new RuntimeException("Lỗi khi gửi email OTP. Vui lòng thử lại sau hoặc liên hệ hỗ trợ.", e);
        } catch (Exception e) {
            log.error("❌ Unexpected error while sending OTP email to: {}", toEmail);
            log.error("Error type: {}", e.getClass().getName());
            log.error("Error message: {}", e.getMessage());
            if (e.getCause() != null) {
                log.error("Root cause: {}", e.getCause().getMessage());
            }
            e.printStackTrace();
            throw new RuntimeException("Đã xảy ra lỗi không mong muốn khi gửi email. Vui lòng thử lại sau.", e);
        }
    }
}

