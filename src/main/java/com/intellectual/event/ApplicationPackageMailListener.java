package com.intellectual.event;

import com.intellectual.model.dto.Result;
import com.intellectual.model.entity.ApplicationPackageActionLog;
import com.intellectual.model.entity.MailSendLog;
import com.intellectual.service.ApplicationPackageActionLogService;
import com.intellectual.service.MailService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class ApplicationPackageMailListener {
    private final MailService mailService;
    private final ApplicationPackageActionLogService actionLogService;

    public ApplicationPackageMailListener(MailService mailService,
                                          ApplicationPackageActionLogService actionLogService) {
        this.mailService = mailService;
        this.actionLogService = actionLogService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onMail(ApplicationPackageMailEvent event) {
        ApplicationPackageActionLog log = actionLogService.getById(event.actionLogId());
        if (log == null) {
            return;
        }
        if (event.recipientEmails() == null || event.recipientEmails().isBlank()) {
            log.setMailStatus("SKIPPED");
            log.setMailError("收件人未配置邮箱");
            actionLogService.updateById(log);
            return;
        }
        try {
            Result result = mailService.sendBusinessMail(
                    event.recipientEmails(), event.subject(), event.content(),
                    "APPLICATION_PACKAGE", event.packageToken(), event.action());
            if (result.getCode() == 200) {
                log.setMailStatus("SUCCESS");
                if (result.getData() instanceof MailSendLog sendLog) {
                    log.setMailLogId(sendLog.getId());
                }
            } else {
                log.setMailStatus("FAILED");
                log.setMailError(result.getMessage());
            }
        } catch (Exception e) {
            log.setMailStatus("FAILED");
            log.setMailError(e.getMessage());
        }
        actionLogService.updateById(log);
    }
}
