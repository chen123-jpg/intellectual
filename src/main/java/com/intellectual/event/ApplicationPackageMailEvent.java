package com.intellectual.event;

public record ApplicationPackageMailEvent(
        Long actionLogId,
        String referenceId,
        String recipientEmails,
        String subject,
        String content,
        String packageToken,
        String action
) {
}
