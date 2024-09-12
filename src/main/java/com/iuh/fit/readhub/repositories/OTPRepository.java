package com.iuh.fit.readhub.repositories;

import com.iuh.fit.readhub.models.OTP;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OTPRepository extends JpaRepository<OTP, Long> {
    Optional<OTP> findByOtpAndEmail(String otp, String email);
}