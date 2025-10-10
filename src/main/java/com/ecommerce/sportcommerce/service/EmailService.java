package com.ecommerce.sportcommerce.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * Service for sending emails
 */
@Service
public class EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
    
    /**
     * Send OTP email
     */
    public void sendOtpEmail(String toEmail, String otpCode, int expirationMinutes) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Mã xác thực OTP - Sport Commerce");
            
            String htmlContent = buildOtpEmailContent(otpCode, expirationMinutes);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            logger.info("OTP email sent successfully to: {}", toEmail);
            
        } catch (MessagingException e) {
            logger.error("Failed to send OTP email to: {}", toEmail, e);
            throw new RuntimeException("Không thể gửi email. Vui lòng thử lại sau");
        }
    }
    
    /**
     * Build OTP email HTML content
     */
    private String buildOtpEmailContent(String otpCode, int expirationMinutes) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "    <meta charset='UTF-8'>" +
                "    <style>" +
                "        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                "        .container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                "        .header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; }" +
                "        .content { background-color: #f9f9f9; padding: 30px; border-radius: 5px; }" +
                "        .otp-code { font-size: 32px; font-weight: bold; color: #4CAF50; text-align: center; " +
                "                    letter-spacing: 5px; padding: 20px; background-color: #fff; " +
                "                    border: 2px dashed #4CAF50; border-radius: 5px; margin: 20px 0; }" +
                "        .footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }" +
                "    </style>" +
                "</head>" +
                "<body>" +
                "    <div class='container'>" +
                "        <div class='header'>" +
                "            <h1>Sport Commerce</h1>" +
                "        </div>" +
                "        <div class='content'>" +
                "            <h2>Xác thực tài khoản</h2>" +
                "            <p>Chào bạn,</p>" +
                "            <p>Bạn đã yêu cầu mã xác thực OTP để hoàn tất đăng ký tài khoản.</p>" +
                "            <p>Mã OTP của bạn là:</p>" +
                "            <div class='otp-code'>" + otpCode + "</div>" +
                "            <p><strong>Lưu ý:</strong></p>" +
                "            <ul>" +
                "                <li>Mã OTP có hiệu lực trong " + expirationMinutes + " phút</li>" +
                "                <li>Không chia sẻ mã này với bất kỳ ai</li>" +
                "                <li>Nếu bạn không yêu cầu mã này, vui lòng bỏ qua email này</li>" +
                "            </ul>" +
                "        </div>" +
                "        <div class='footer'>" +
                "            <p>&copy; 2024 Sport Commerce. All rights reserved.</p>" +
                "        </div>" +
                "    </div>" +
                "</body>" +
                "</html>";
    }
    
    /**
     * Send password reset email
     */
    public void sendPasswordResetEmail(String toEmail, String otpCode, int expirationMinutes) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Đặt lại mật khẩu - Sport Commerce");
            
            String htmlContent = buildPasswordResetEmailContent(otpCode, expirationMinutes);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            logger.info("Password reset email sent successfully to: {}", toEmail);
            
        } catch (MessagingException e) {
            logger.error("Failed to send password reset email to: {}", toEmail, e);
            throw new RuntimeException("Không thể gửi email. Vui lòng thử lại sau");
        }
    }
    
    /**
     * Build password reset email HTML content
     */
    private String buildPasswordResetEmailContent(String otpCode, int expirationMinutes) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "    <meta charset='UTF-8'>" +
                "    <style>" +
                "        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                "        .container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                "        .header { background-color: #f44336; color: white; padding: 20px; text-align: center; }" +
                "        .content { background-color: #f9f9f9; padding: 30px; border-radius: 5px; }" +
                "        .otp-code { font-size: 32px; font-weight: bold; color: #f44336; text-align: center; " +
                "                    letter-spacing: 5px; padding: 20px; background-color: #fff; " +
                "                    border: 2px dashed #f44336; border-radius: 5px; margin: 20px 0; }" +
                "        .footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }" +
                "    </style>" +
                "</head>" +
                "<body>" +
                "    <div class='container'>" +
                "        <div class='header'>" +
                "            <h1>Sport Commerce</h1>" +
                "        </div>" +
                "        <div class='content'>" +
                "            <h2>Đặt lại mật khẩu</h2>" +
                "            <p>Chào bạn,</p>" +
                "            <p>Bạn đã yêu cầu đặt lại mật khẩu tài khoản.</p>" +
                "            <p>Mã xác thực của bạn là:</p>" +
                "            <div class='otp-code'>" + otpCode + "</div>" +
                "            <p><strong>Lưu ý:</strong></p>" +
                "            <ul>" +
                "                <li>Mã xác thực có hiệu lực trong " + expirationMinutes + " phút</li>" +
                "                <li>Không chia sẻ mã này với bất kỳ ai</li>" +
                "                <li>Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này</li>" +
                "            </ul>" +
                "        </div>" +
                "        <div class='footer'>" +
                "            <p>&copy; 2024 Sport Commerce. All rights reserved.</p>" +
                "        </div>" +
                "    </div>" +
                "</body>" +
                "</html>";
    }
}
