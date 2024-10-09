package com.iuh.fit.readhub.dto;

import com.iuh.fit.readhub.constants.SuccessCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse <T> {
    private T data;
    private String message;
    private int status;
    private boolean success;
    private T error;
}