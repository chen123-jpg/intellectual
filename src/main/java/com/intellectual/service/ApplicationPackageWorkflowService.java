package com.intellectual.service;

import com.intellectual.model.dto.ApplicationPackageWorkflowDtos.BatchView;
import com.intellectual.model.dto.ApplicationPackageWorkflowDtos.DownloadTicketView;
import com.intellectual.model.dto.ApplicationPackageWorkflowDtos.ProcessOperatorView;
import com.intellectual.model.dto.ApplicationPackageWorkflowDtos.RejectRequest;
import com.intellectual.security.LoginUser;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface ApplicationPackageWorkflowService {
    Map<String, Object> list(int pageNum, int pageSize, String status, String internalNo,
                             String disclosureName, String sponsorName, LoginUser actor);
    BatchView get(String packageToken, LoginUser actor);
    BatchView getByDisclosure(Long disclosureId, LoginUser actor);
    BatchView createDraft(Long disclosureId, LoginUser actor);
    BatchView uploadFile(String packageToken, String documentCode, MultipartFile file, LoginUser actor);
    BatchView removeFile(String packageToken, String documentCode, LoginUser actor);
    BatchView send(String packageToken, Long processUserId, LoginUser actor);
    BatchView receive(String packageToken, LoginUser actor);
    BatchView reject(String packageToken, RejectRequest request, LoginUser actor);
    BatchView approve(String packageToken, LoginUser actor);
    BatchView unlock(String packageToken, String reason, LoginUser actor);
    BatchView submitCnipa(String packageToken, String submissionNo, Date submittedAt,
                          MultipartFile receipt, LoginUser actor);
    List<ProcessOperatorView> processOperators(LoginUser actor);
    DownloadTicketView createDownloadTicket(String fileToken, LoginUser actor);
    ResponseEntity<Resource> download(String ticket, LoginUser actor);
}
