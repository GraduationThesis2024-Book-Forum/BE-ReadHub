package com.iuh.fit.readhub.constants;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    EMAIL_ALREADY_EXISTS(409, "Mail Already Exists!", HttpStatus.CONFLICT),
    LOGIN_FAILED(400, "Invalid username or password!", HttpStatus.BAD_REQUEST);

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}
