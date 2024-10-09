package com.iuh.fit.readhub.dto;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@ToString
public class RegistrationResponse {
    private boolean success;
    private String message;
    private String role;
    private String token;
}