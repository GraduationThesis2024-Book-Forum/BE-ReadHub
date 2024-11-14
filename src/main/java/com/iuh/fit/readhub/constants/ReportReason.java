package com.iuh.fit.readhub.constants;

public enum ReportReason {
    INAPPROPRIATE_CONTENT("Inappropriate book forum content"),
    ADULT_CONTENT("Adult/NSFW content"),
    SPAM("Spam/Advertising"),
    HATE_SPEECH("Hate speech/Discrimination"),
    VIOLENCE("Violence/Dangerous content"),
    COPYRIGHT("Copyright violation"),
    HARASSMENT("Harassment/Threats"),
    MISINFORMATION("False information about books/authors"),
    OFF_TOPIC("Irrelevant to books/reading"),
    TROLLING("Trolling/Disrupting discussions"),
    IMPERSONATION("Impersonating others"),
    PERSONAL_INFO("Sharing personal information"),
    OTHER("Other reasons");

    private final String description;

    ReportReason(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}