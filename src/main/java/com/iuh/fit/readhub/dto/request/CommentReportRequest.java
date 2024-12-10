package com.iuh.fit.readhub.dto.request;

import com.iuh.fit.readhub.constants.CommentReportReason;
import lombok.Data;

@Data
public class CommentReportRequest {
    private CommentReportReason reason;
    private String additionalInfo;
}