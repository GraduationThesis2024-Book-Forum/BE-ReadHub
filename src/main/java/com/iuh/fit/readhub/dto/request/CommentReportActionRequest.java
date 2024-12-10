package com.iuh.fit.readhub.dto.request;

import com.iuh.fit.readhub.constants.ReportAction;
import lombok.Data;

@Data
public class CommentReportActionRequest {
    private ReportAction action;
    private String reason;
    private CommentBanTypes banTypes;

    @Data
    public static class CommentBanTypes {
        private boolean noComment;
        private boolean deleteComment;
    }
}