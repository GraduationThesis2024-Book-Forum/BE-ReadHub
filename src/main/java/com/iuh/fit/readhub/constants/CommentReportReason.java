package com.iuh.fit.readhub.constants;

public enum CommentReportReason {
    INAPPROPRIATE("Inappropriate comment content"),
    HARASSMENT("Harassment or bullying"),
    HATE_SPEECH("Hate speech/discrimination"),
    SPAM("Spam or advertising"),
    MISINFORMATION("False or misleading information"),
    OFF_TOPIC("Off-topic/irrelevant"),
    PERSONAL_ATTACK("Personal attack"),
    TROLLING("Trolling/disruptive behavior"),
    IMPERSONATION("Impersonating others"),
    ADULT_CONTENT("Adult/NSFW content"),
    VIOLENCE("Violence/threats"),
    PERSONAL_INFO("Sharing personal information"),
    OTHER("Other reasons");

    private final String description;

    CommentReportReason(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
