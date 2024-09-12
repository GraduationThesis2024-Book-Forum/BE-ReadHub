package com.iuh.fit.readhub.services;

import com.iuh.fit.readhub.models.OTP;
import com.iuh.fit.readhub.repositories.OTPRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class OtpService {
    @Autowired
    private OTPRepository otpRepository;

    public OTP generateOtp(String email) {
        String otpCode = UUID.randomUUID().toString().substring(0, 6); // Tạo mã OTP 6 ký tự
        OTP otp = new OTP();
        otp.setOtp(otpCode);
        otp.setEmail(email);
        otp.setExpiryDate(LocalDateTime.now().plusMinutes(10)); // OTP hết hạn sau 10 phút

        otpRepository.save(otp);
        return otp;
    }

    public boolean validateOtp(String otpCode, String email) {
        Optional<OTP> otp = otpRepository.findByOtpAndEmail(otpCode, email);
        if (otp.isPresent() && otp.get().getExpiryDate().isAfter(LocalDateTime.now())) {
            return true;
        }
        return false;
    }
}

