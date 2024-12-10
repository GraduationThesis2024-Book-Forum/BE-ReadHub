package com.iuh.fit.readhub.dto;

import com.iuh.fit.readhub.constants.CommentReportReason;
import com.iuh.fit.readhub.constants.ReportStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentReportDTO {
    private Long id;
    private Long commentId;
    private String commentContent;
    private UserDTO reporter;
    private UserDTO commentAuthor;
    private CommentReportReason reason;
    private String additionalInfo;
    private ReportStatus status;
    private LocalDateTime reportedAt;
    private LocalDateTime resolvedAt;
    private String imageUrl;
    private String discussionTitle;
}