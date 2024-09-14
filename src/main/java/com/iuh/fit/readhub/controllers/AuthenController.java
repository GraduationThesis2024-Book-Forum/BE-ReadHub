package com.iuh.fit.readhub.controllers;


import com.iuh.fit.readhub.constants.ValidationMessages;
import com.iuh.fit.readhub.dto.RegistrationResponse;
import com.iuh.fit.readhub.models.OTP;
import com.iuh.fit.readhub.services.AuthenService;
import com.iuh.fit.readhub.services.EmailService;
import com.iuh.fit.readhub.services.OtpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<?> registerUser(@RequestParam String email) {
        try {
            OTP otp = otpService.generateOtp(email);
            emailService.sendOtpEmail(email, otp.getOtp());
            return ResponseEntity.ok("OTP sent successfully. \n"+otp.getOtp());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to send OTP: " + e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> registerAndVerify(
            @RequestParam String email,
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam(required = false) String otp
    ) {

        if (otp != null && !otp.isEmpty()) {
            boolean isOtpValid = otpService.validateOtp(otp, email);
            if (!isOtpValid) {
                RegistrationResponse errorResponse = new RegistrationResponse(false, "Mã OTP không hợp lệ hoặc đã hết hạn", null);
                return ResponseEntity.badRequest().body(errorResponse);
            }
        }
        if (otp == null || otp.isEmpty()) {
            RegistrationResponse response = new RegistrationResponse(false, "Đăng ký thành công. Vui lòng xác thực tài khoản của bạn bằng cách nhập mã OTP đã gửi đến email của bạn.", null);
            return ResponseEntity.ok(response);
        }
        RegistrationResponse registrationResponse = authService.registerForUser(email, username, password);
        if (!registrationResponse.isSuccess()) {
            if (registrationResponse.getMessage().equals(ValidationMessages.EMAIL_ALREADY_EXISTS.getMessage())) {
                RegistrationResponse errorResponse = new RegistrationResponse(false, "Email đã tồn tại", null);
                return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
            } else {
                RegistrationResponse errorResponse = new RegistrationResponse(false, "Đăng ký thất bại: " + registrationResponse.getMessage(), null);
                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
            }
        }
        return new ResponseEntity<>(registrationResponse, HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String email, @RequestParam String password) {
        String token = authService.login(email, password);
        if (token != null) {
            return ResponseEntity.ok(new RegistrationResponse(true, "Đăng nhập thành công", token));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RegistrationResponse(false, "Email hoặc mật khẩu không đúng", null));
        }
    }
}
