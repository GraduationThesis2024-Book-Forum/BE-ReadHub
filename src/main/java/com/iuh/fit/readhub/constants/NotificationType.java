package com.iuh.fit.readhub.constants;

import lombok.Getter;

@Getter
public enum NotificationType {
    FORUM_REPORT("New Forum Report", "A forum has been reported"),
    REPORT_ACTION("Forum Report Update", null), // Message from ReportAction enum
    NEW_MEMBER("New Forum Member", "%s has joined your forum"),
    NEW_COMMENT("New Comment", "%s commented on your forum"),
    FORUM_LIKE("Forum Like", "%s liked your forum");

    private final String title;
    private final String messageTemplate;

    NotificationType(String title, String messageTemplate) {
        this.title = title;
        this.messageTemplate = messageTemplate;
    }

    public String formatMessage(Object... args) {
        return messageTemplate != null ? String.format(messageTemplate, args) : null;
    }
}