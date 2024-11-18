package com.iuh.fit.readhub.controllers;


import com.iuh.fit.readhub.constants.ValidationMessages;
import com.iuh.fit.readhub.dto.RegistrationResponse;
import com.iuh.fit.readhub.models.OTP;
import com.iuh.fit.readhub.services.AuthenService;
import com.iuh.fit.readhub.services.EmailService;
import com.iuh.fit.readhub.services.OtpService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("api/v1/authen")
public class AuthenController {
    @Autowired
    private AuthenService authService;

    @Autowired
    private OtpService otpService;

    @Autowired
    private EmailService emailService;

    @PostMapping("/send-otp")
    public ResponseEntity<RegistrationResponse> registerUser(
            @RequestParam String email,
            @RequestParam String username
    ) throws MessagingException {
            RegistrationResponse registrationResponse = authService.sendOTPToRegister(email, username);
            if (!registrationResponse.isSuccess()) {
                return new ResponseEntity<>(registrationResponse, HttpStatus.CONFLICT);
            }
            OTP otp = otpService.generateOtp(email);
            emailService.sendOtpEmail(email, otp.getOtp());
            return new ResponseEntity<>(registrationResponse, HttpStatus.OK);
    }

    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> registerAndVerify(
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam(required = false) String otp
    ) {
        if (otp != null && !otp.isEmpty()) {
            boolean isOtpValid = otpService.validateOtp(otp, email);
            if (!isOtpValid) {
                RegistrationResponse errorResponse = new RegistrationResponse(false, ValidationMessages.OTP_IS_OUTDATED.getMessage(), null,null);
                return ResponseEntity.badRequest().body(errorResponse);
            }
        }
        if (otp == null || otp.isEmpty()) {
            RegistrationResponse response = new RegistrationResponse(false, ValidationMessages.OTP_NOT_EMPTY.getMessage(), null,null);
            return ResponseEntity.ok(response);
        }
        RegistrationResponse registrationResponse = authService.registerForUser(email,fullName, username, password);
        if (!registrationResponse.isSuccess()) {
            if (registrationResponse.getMessage().equals(ValidationMessages.EMAIL_ALREADY_EXISTS.getMessage())) {
                return new ResponseEntity<>(registrationResponse, HttpStatus.CONFLICT);
            } else {
                return new ResponseEntity<>(registrationResponse, HttpStatus.BAD_REQUEST);
            }
        }
        return new ResponseEntity<>(registrationResponse, HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String email, @RequestParam String password, HttpServletResponse response) {
        String token = authService.login(email, password);
        String role= authService.getRole(email);
        if (token != null) {
            return ResponseEntity.ok(new RegistrationResponse(true, "Đăng nhập thành công", role, token));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RegistrationResponse(false, "Tài khoản hoặc mật khẩu không đúng", null, null));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("token", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        Map<String, String> model = new HashMap<>();
        model.put("message", "Logged out successfully");
        return ResponseEntity.ok(model);
    }
}
