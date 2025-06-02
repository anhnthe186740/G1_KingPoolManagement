package com.example.kingpool.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendOtpEmail(String to, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("🔐 Mã OTP đặt lại mật khẩu - KingPool");

            String htmlContent = "<div style='font-family: Arial, sans-serif; color: #333; font-size: 15px;'>"
                    + "<h2 style='color: #007bff;'>🔐 Xác nhận đặt lại mật khẩu</h2>"
                    + "<p>Chào bạn,</p>"
                    + "<p>Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn.</p>"
                    + "<p style='font-size: 18px; margin: 20px 0;'>Mã OTP của bạn là:</p>"
                    + "<div style='text-align: center; font-size: 28px; color: #e74c3c; font-weight: bold;'>"
                    + otp + "</div>"
                    + "<p style='margin-top: 20px;'>Mã có hiệu lực trong <strong>5 phút</strong>.</p>"
                    + "<hr style='margin: 30px 0;'>"
                    + "<p style='font-size: 13px; color: #888;'>Nếu bạn không yêu cầu đặt lại, hãy bỏ qua email này.</p>"
                    + "<p style='font-size: 12px;'>Trân trọng,<br><strong>KingPool Team</strong></p>"
                    + "</div>";

            helper.setText(htmlContent, true);
            mailSender.send(message);
            System.out.println("✅ Đã gửi email HTML tới: " + to);
        } catch (Exception e) {
            System.err.println("❌ Gửi email HTML thất bại: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
