package com.iuh.fit.readhub.dto;

import com.iuh.fit.readhub.constants.ReportReason;
import com.iuh.fit.readhub.constants.ReportStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ForumReportDTO {
    private Long id;
    private ForumDTO forum;
    private UserDTO reporter;
    private ReportReason reason;
    private String additionalInfo;
    private ReportStatus status;
    private LocalDateTime reportedAt;
    private LocalDateTime resolvedAt;
}