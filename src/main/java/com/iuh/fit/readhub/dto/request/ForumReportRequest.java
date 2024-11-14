package com.iuh.fit.readhub.dto.request;

import com.iuh.fit.readhub.constants.ReportReason;
import lombok.Data;

@Data
public class ForumReportRequest {
    private ReportReason reason;
    private String additionalInfo;
}