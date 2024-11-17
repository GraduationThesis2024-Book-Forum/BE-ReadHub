package com.iuh.fit.readhub.dto.request;

import com.iuh.fit.readhub.constants.ReportAction;
import lombok.Data;

@Data
public class ReportActionRequest {
    private ReportAction action;
    private String reason;
}