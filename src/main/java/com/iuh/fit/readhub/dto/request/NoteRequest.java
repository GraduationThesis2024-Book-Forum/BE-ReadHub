package com.iuh.fit.readhub.dto.request;

import com.iuh.fit.readhub.models.User;
import jakarta.persistence.Column;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NoteRequest {
    private Long noteId;
    private Long userId;
    private Long bookId;
    private String content;
    private String selectedText;
    private String cfiRange;
    private String color;
}
