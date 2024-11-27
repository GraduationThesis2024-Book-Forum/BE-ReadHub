package com.iuh.fit.readhub.dto.request;

import com.iuh.fit.readhub.constants.ReportAction;
import lombok.Data;

@Data
public class ReportActionRequest {
    private ReportAction action;
    private String reason;
    private BanTypes banTypes;

    @Data
    public static class BanTypes {
        private boolean noInteraction;
        private boolean noComment;
        private boolean noJoin;
        private boolean isNoForumCreation;
        private boolean deleteForum;
    }
}