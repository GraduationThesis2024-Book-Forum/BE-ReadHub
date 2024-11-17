package com.iuh.fit.readhub.dto.request;

import lombok.Data;

import java.util.Map;

@Data
public class NotificationRequest {
    private Long userId;
    private String title;
    private String body;
    private Map<String, String> data;
}