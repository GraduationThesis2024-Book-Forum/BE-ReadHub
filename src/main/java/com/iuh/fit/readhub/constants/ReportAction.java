package com.iuh.fit.readhub.constants;

import lombok.Getter;

@Getter
public enum ReportAction {
    DISMISS("Your forum report has been dismissed"),
    WARN("You have received a warning for your forum content"),
    BAN_1H("Your forum creation privileges have been suspended for 1 hour", 1),
    BAN_3H("Your forum creation privileges have been suspended for 3 hours", 3),
    BAN_24H("Your forum creation privileges have been suspended for 24 hours", 24),
    BAN_PERMANENT("Your forum creation privileges have been permanently suspended", -1);

    private final String notificationMessage;
    private final int banHours; // -1 for permanent ban

    ReportAction(String notificationMessage) {
        this.notificationMessage = notificationMessage;
        this.banHours = 0;
    }

    ReportAction(String notificationMessage, int banHours) {
        this.notificationMessage = notificationMessage;
        this.banHours = banHours;
    }
}