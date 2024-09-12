package com.iuh.fit.readhub.constants;

public enum ValidationMessages {
    EMAIL_ALREADY_EXISTS("Email đã được đăng ký"),
    EMAIL_INVALID("Email không hợp lệ"),
    PASSWORD_INVALID("Password phải chứa ít nhất 8 ký tự, bao gồm chữ cái, chữ số và ký tự đặc biệt"),
    USERNAME_INVALID("Username phải có ít nhất 6 ký tự"),
    REGISTER_SUCCESS("Đăng ký thành công!"),
    USERNAME_ALREADY_EXISTS("Username đã tồn tại"),
    OTP_NOT_EMPTY("Mã OTP không được để trống"),
    OTP_IS_OUTDATED("Mã OTP này không còn hiệu lực"),
    OTP_IS_INVALID("Mã OTP không hợp lệ");

    private final String message;

    ValidationMessages(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}