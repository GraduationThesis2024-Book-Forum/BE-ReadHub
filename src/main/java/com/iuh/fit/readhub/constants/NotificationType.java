package com.iuh.fit.readhub.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NotificationType {
    FORUM_REPORT("New Forum Report", "A forum has been reported"),
    REPORT_ACTION("Forum Report Update", "%s"), // Takes the message from ReportAction enum
    NEW_MEMBER("New Forum Member", "%s has joined your forum"),
    NEW_COMMENT("New Comment", "%s commented on your forum"),
    FORUM_LIKE("Forum Like", "%s liked your forum"),
    PERMANENT_BAN("Permanent Ban", "You have been permanently banned from the forum: %s"), // Add reason
    BAN("Temporary Ban", "You have been banned from forum interactions for %d hours: %s"), // Add hours and reason
    WARNING("Warning", "%s"), // Takes warning message directly
    BAN_EXPIRED("Ban Expired", "Your forum interaction ban has expired"),
    BAN_NOTIFICATION("Forum Access Restricted", "You cannot interact with forums while banned");

    private final String title;
    private final String messageTemplate;

    public String formatMessage(Object... args) {
        if (messageTemplate == null) return null;
        try {
            return String.format(messageTemplate, args);
        } catch (Exception e) {
            return messageTemplate;
        }
    }
}