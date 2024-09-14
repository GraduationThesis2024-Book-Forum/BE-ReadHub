package com.iuh.fit.readhub.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendOtpEmail(String to, String otp) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        String htmlMsg = "<div style='font-family: Arial, sans-serif;'>"
                + "<p>Cảm ơn bạn đã đăng ký tài khoản tại READHUB, ứng dụng đọc sách yêu thích của bạn.</p>"
                + "<p>Dưới đây là mã OTP để xác nhận đăng ký tài khoản của bạn:</p>"
                + "<div style='border: 2px solid #4CAF50; padding: 20px; text-align: center;'>"
                + "<h2 style='font-size: 24px;'>Mã OTP của bạn</h2>"
                + "<div style='font-size: 24px; font-weight: bold; padding: 10px; background-color: #f9f9f9; border: 2px solid #ddd;'>"
                + otp
                + "</div>"
                + "</div>"
                + "<p>Lưu ý: Mã OTP này chỉ có hiệu lực trong vòng 10 phút. Vui lòng không chia sẻ mã OTP này với bất kỳ ai.</p>"
                + "<p>Chúc bạn có những trải nghiệm thú vị với READHUB!</p>"
                + "<p>Trân trọng,<br>Đội ngũ READHUB</p>"
                + "</div>";

        helper.setTo(to);
        helper.setSubject("Your OTP Code for READHUB");
        helper.setText(htmlMsg, true);

        mailSender.send(message);
    }
}
