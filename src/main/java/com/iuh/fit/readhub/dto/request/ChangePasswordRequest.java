package com.iuh.fit.readhub.dto.request;

import lombok.Data;

@Data
public class ChangePasswordRequest {
    private String password;
    private String newPassword;
}
