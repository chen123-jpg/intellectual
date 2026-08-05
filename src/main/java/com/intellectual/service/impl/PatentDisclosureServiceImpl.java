package com.intellectual.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.intellectual.exception.BusinessException;
import com.intellectual.mapper.DisclosureAttachmentMapper;
import com.intellectual.mapper.FeePaymentMapper;
import com.intellectual.mapper.InvoiceMapper;
import com.intellectual.mapper.PatentDisclosureMapper;
import com.intellectual.model.dto.PatentDisclosureDTO;
import com.intellectual.model.dto.Result;
import com.intellectual.model.entity.DisclosureAttachment;
import com.intellectual.model.entity.FeePayment;
import com.intellectual.model.entity.Invoice;
import com.intellectual.model.entity.PatentDisclosure;
import com.intellectual.service.PatentDisclosureService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.intellectual.model.constants.TtableConstant.DISCLOSURE_DOC;
import static com.intellectual.model.constants.TtableConstant.DISCLOSURE_OTHER;
import static com.intellectual.model.constants.TtableConstant.WORD_EXTENSIONS;

/**
 * 专利交底信息表（T表） 服务实现类
 *
 * @author 陈创
 * @since 2026-07-23 16:59
 */
@Service
public class PatentDisclosureServiceImpl extends ServiceImpl<PatentDisclosureMapper, PatentDisclosure> implements PatentDisclosureService {

    private static final String DISCLOSURE_SYNC = "DISCLOSURE_SYNC";
    private static final String TEMP_NO_PREFIX = "P";
    private static final ZoneId BUSINESS_ZONE_ID = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter TEMP_NO_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final PatentDisclosureMapper patentDisclosureMapper;
    private final DisclosureAttachmentMapper disclosureAttachmentMapper;
    private final FeePaymentMapper feePaymentMapper;
    private final InvoiceMapper invoiceMapper;
    private final UploadFileServiceImpl uploadFileService;

    public PatentDisclosureServiceImpl(PatentDisclosureMapper patentDisclosureMapper,
                                       DisclosureAttachmentMapper disclosureAttachmentMapper,
                                       FeePaymentMapper feePaymentMapper,
                                       InvoiceMapper invoiceMapper,
                                       UploadFileServiceImpl uploadFileService) {
        this.patentDisclosureMapper = patentDisclosureMapper;
        this.disclosureAttachmentMapper = disclosureAttachmentMapper;
        this.feePaymentMapper = feePaymentMapper;
        this.invoiceMapper = invoiceMapper;
        this.uploadFileService = uploadFileService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PatentDisclosure createWithAttachments(PatentDisclosureDTO request,
                                                  List<MultipartFile> disclosureDocuments,
                                                  List<MultipartFile> otherAttachments,
                                                  Long sourceId,
                                                  Long uploadUserId,
                                                  String uploadUserName) {
        validateDisclosureDocument(disclosureDocuments);

        PatentDisclosure disclosure = new PatentDisclosure();
        BeanUtils.copyProperties(request, disclosure, "id", "patentType");
        disclosure.setTempNo(generateTempNo(disclosure.getDisclosureDate()));
        disclosure.setCopyFromId(sourceId);
        disclosure.setEntryUserId(uploadUserId);
        disclosure.setEntryUserName(uploadUserName);

        if (patentDisclosureMapper.insert(disclosure) != 1 || disclosure.getId() == null) {
            throw new BusinessException("交底信息保存失败");
        }

        List<String> uploadedFileUrls = new ArrayList<>();
        boolean cleanupRegistered = registerRollbackCleanup(uploadedFileUrls);
        try {
            List<DisclosureAttachment> attachments = new ArrayList<>();
            attachments.add(storeAttachment(disclosureDocuments.get(0), DISCLOSURE_DOC,
                    uploadUserId, uploadUserName, 0, uploadedFileUrls));
            if (otherAttachments != null) {
                int sortNo = 1;
                for (MultipartFile file : otherAttachments) {
                    if (file != null && !file.isEmpty()) {
                        attachments.add(storeAttachment(file, DISCLOSURE_OTHER,
                                uploadUserId, uploadUserName, sortNo++, uploadedFileUrls));
                    }
                }
            }

            for (DisclosureAttachment attachment : attachments) {
                attachment.setDisclosureId(disclosure.getId());
                attachment.setInternalNo(disclosure.getInternalNo());
                if (disclosureAttachmentMapper.insert(attachment) != 1) {
                    throw new BusinessException("交底附件保存失败");
                }
            }
            if (feePaymentMapper.insert(buildFeePayment(disclosure)) != 1) {
                throw new BusinessException("缴费信息同步失败");
            }
            if (invoiceMapper.insert(buildInvoice(disclosure)) != 1) {
                throw new BusinessException("开票信息同步失败");
            }
            return disclosure;
        } catch (RuntimeException | Error ex) {
            if (!cleanupRegistered) {
                cleanupUploadedFiles(uploadedFileUrls);
            }
            throw ex;
        }
    }

    private void validateDisclosureDocument(List<MultipartFile> disclosureDocuments) {
        if (disclosureDocuments == null || disclosureDocuments.size() != 1
                || disclosureDocuments.get(0) == null || disclosureDocuments.get(0).isEmpty()) {
            throw new BusinessException("交底书必须且只能上传一份");
        }
        if (!isWordDocument(disclosureDocuments.get(0))) {
            throw new BusinessException("交底书只能上传 .doc 或 .docx 格式的 Word 文档");
        }
    }

    private DisclosureAttachment storeAttachment(MultipartFile file,
                                                  String bizType,
                                                  Long uploadUserId,
                                                  String uploadUserName,
                                                  int sortNo,
                                                  List<String> uploadedFileUrls) {
        Result<String> uploadResult = uploadFileService.upload(file);
        if (uploadResult.getCode() != 200 || uploadResult.getData() == null) {
            throw new BusinessException(uploadResult.getMessage());
        }
        String fileUrl = uploadResult.getData();
        uploadedFileUrls.add(fileUrl);

        DisclosureAttachment attachment = new DisclosureAttachment();
        attachment.setBizType(bizType);
        attachment.setFileName(file.getOriginalFilename());
        attachment.setFileExt(getFileExtension(file));
        attachment.setFilePath(fileUrl);
        attachment.setFileUrl(fileUrl);
        attachment.setFileSize(file.getSize());
        attachment.setContentType(file.getContentType());
        attachment.setIsRequired(DISCLOSURE_DOC.equals(bizType) ? 1 : 0);
        attachment.setSortNo(sortNo);
        attachment.setUploadUserId(uploadUserId);
        attachment.setUploadUserName(uploadUserName);
        attachment.setDeleted(0);
        return attachment;
    }

    private boolean registerRollbackCleanup(List<String> uploadedFileUrls) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return false;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status != TransactionSynchronization.STATUS_COMMITTED) {
                    cleanupUploadedFiles(uploadedFileUrls);
                }
            }
        });
        return true;
    }

    private void cleanupUploadedFiles(List<String> uploadedFileUrls) {
        for (String fileUrl : uploadedFileUrls) {
            uploadFileService.deleteByUrl(fileUrl);
        }
    }

    private boolean isWordDocument(MultipartFile file) {
        return file != null && !file.isEmpty() && WORD_EXTENSIONS.contains(getFileExtension(file));
    }

    private String getFileExtension(MultipartFile file) {
        String filename = file != null ? file.getOriginalFilename() : null;
        if (filename == null) {
            return "";
        }
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DisclosureAttachment replaceDisclosureDocument(Long disclosureId,
                                                           String internalNo,
                                                           MultipartFile file,
                                                           Long uploadUserId,
                                                           String uploadUserName) {
        validateDisclosureDocument(List.of(file));
        Long activeDocumentCount = disclosureAttachmentMapper.selectCount(
                Wrappers.<DisclosureAttachment>lambdaQuery()
                        .eq(DisclosureAttachment::getDisclosureId, disclosureId)
                        .eq(DisclosureAttachment::getBizType, DISCLOSURE_DOC)
                        .eq(DisclosureAttachment::getDeleted, 0));

        List<String> uploadedFileUrls = new ArrayList<>();
        boolean cleanupRegistered = registerRollbackCleanup(uploadedFileUrls);
        try {
            DisclosureAttachment replacement = storeAttachment(
                    file, DISCLOSURE_DOC, uploadUserId, uploadUserName, 0, uploadedFileUrls);
            replacement.setDisclosureId(disclosureId);
            replacement.setInternalNo(internalNo);
            if (disclosureAttachmentMapper.insert(replacement) != 1 || replacement.getId() == null) {
                throw new BusinessException("交底书更换失败");
            }

            int retiredDocumentCount = disclosureAttachmentMapper.update(null,
                    Wrappers.<DisclosureAttachment>lambdaUpdate()
                            .set(DisclosureAttachment::getDeleted, 1)
                            .set(DisclosureAttachment::getUpdateTime, new Date())
                            .eq(DisclosureAttachment::getDisclosureId, disclosureId)
                            .eq(DisclosureAttachment::getBizType, DISCLOSURE_DOC)
                            .eq(DisclosureAttachment::getDeleted, 0)
                            .ne(DisclosureAttachment::getId, replacement.getId()));
            if (activeDocumentCount != null && retiredDocumentCount != activeDocumentCount.intValue()) {
                throw new BusinessException("旧交底书更新失败");
            }
            return replacement;
        } catch (RuntimeException | Error ex) {
            if (!cleanupRegistered) {
                cleanupUploadedFiles(uploadedFileUrls);
            }
            throw ex;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateWithRelatedRecords(PatentDisclosure disclosure) {
        disclosure.setTempNo(generateTempNo(disclosure.getDisclosureDate()));
        if (patentDisclosureMapper.updateById(disclosure) != 1) {
            return false;
        }

        PatentDisclosure current = patentDisclosureMapper.selectById(disclosure.getId());
        if (current == null) {
            throw new BusinessException("交底记录不存在");
        }
        syncFeePayment(current);
        syncInvoice(current);
        return true;
    }

    /**
     * 根据日期自动生成临时编号
     * @param disclosureDate
     * @return
     */
    private String generateTempNo(Date disclosureDate) {
        if (disclosureDate == null) {
            throw new BusinessException("交底日期不能为空");
        }
        String datePart = Instant.ofEpochMilli(disclosureDate.getTime())
                .atZone(BUSINESS_ZONE_ID)
                .toLocalDate()
                .format(TEMP_NO_DATE_FORMATTER);
        return TEMP_NO_PREFIX + datePart;
    }

    private void syncFeePayment(PatentDisclosure disclosure) {
        Long count = feePaymentMapper.selectCount(
                Wrappers.<FeePayment>lambdaQuery()
                        .eq(FeePayment::getDisclosureId, disclosure.getId())
                        .eq(FeePayment::getSource, DISCLOSURE_SYNC));
        if (count == 0) {
            feePaymentMapper.insert(buildFeePayment(disclosure));
            return;
        }
        feePaymentMapper.update(null,
                Wrappers.<FeePayment>lambdaUpdate()
                        .set(FeePayment::getInternalNo, disclosure.getInternalNo())
                        .set(FeePayment::getTempNo, disclosure.getTempNo())
                        .set(FeePayment::getDisclosureName, disclosure.getDisclosureName())
                        .set(FeePayment::getApplicant, disclosure.getApplicant())
                        .set(FeePayment::getUpdateTime, new Date())
                        .eq(FeePayment::getDisclosureId, disclosure.getId())
                        .eq(FeePayment::getSource, DISCLOSURE_SYNC));
    }

    private void syncInvoice(PatentDisclosure disclosure) {
        Long count = invoiceMapper.selectCount(
                Wrappers.<Invoice>lambdaQuery()
                        .eq(Invoice::getDisclosureId, disclosure.getId())
                        .eq(Invoice::getSource, DISCLOSURE_SYNC));
        if (count == 0) {
            invoiceMapper.insert(buildInvoice(disclosure));
            return;
        }
        invoiceMapper.update(null,
                Wrappers.<Invoice>lambdaUpdate()
                        .set(Invoice::getInternalNo, disclosure.getInternalNo())
                        .set(Invoice::getTempNo, disclosure.getTempNo())
                        .set(Invoice::getDisclosureName, disclosure.getDisclosureName())
                        .set(Invoice::getApplicant, disclosure.getApplicant())
                        .set(Invoice::getUpdateTime, new Date())
                        .eq(Invoice::getDisclosureId, disclosure.getId())
                        .eq(Invoice::getSource, DISCLOSURE_SYNC));
    }

    private FeePayment buildFeePayment(PatentDisclosure disclosure) {
        Date now = new Date();
        FeePayment fee = new FeePayment();
        fee.setDisclosureId(disclosure.getId());
        fee.setInternalNo(disclosure.getInternalNo());
        fee.setTempNo(disclosure.getTempNo());
        fee.setDisclosureName(disclosure.getDisclosureName());
        fee.setApplicant(disclosure.getApplicant());
        fee.setPayer(disclosure.getApplicant());
        fee.setPaymentStatus("PENDING");
        fee.setSource(DISCLOSURE_SYNC);
        fee.setCreateTime(now);
        fee.setUpdateTime(now);
        return fee;
    }

    private Invoice buildInvoice(PatentDisclosure disclosure) {
        Date now = new Date();
        Invoice invoice = new Invoice();
        invoice.setDisclosureId(disclosure.getId());
        invoice.setInternalNo(disclosure.getInternalNo());
        invoice.setTempNo(disclosure.getTempNo());
        invoice.setDisclosureName(disclosure.getDisclosureName());
        invoice.setApplicant(disclosure.getApplicant());
        invoice.setInvoiceTitle(disclosure.getApplicant());
        invoice.setInvoiceStatus("PENDING");
        invoice.setSource(DISCLOSURE_SYNC);
        invoice.setCreateTime(now);
        invoice.setUpdateTime(now);
        return invoice;
    }
}
