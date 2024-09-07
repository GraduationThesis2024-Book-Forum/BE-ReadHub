package com.iuh.fit.readhub.controllers;

import com.iuh.fit.readhub.constants.ValidationConstants;
import com.iuh.fit.readhub.constants.ValidationMessages;
import com.iuh.fit.readhub.dto.RegistrationResponse;
import com.iuh.fit.readhub.services.AuthenService;
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

    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> registerForUser(@RequestParam String email, @RequestParam String username, @RequestParam String password) {
        RegistrationResponse response = authService.registerForUser(email, username, password);

        if (response.isSuccess()) {
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else if (response.getMessage().equals(ValidationMessages.EMAIL_ALREADY_EXISTS.getMessage())) {
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        } else {
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
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
