package com.intellectual.service;

import com.intellectual.model.dto.PatentDisclosureDTO;
import com.intellectual.model.entity.DisclosureAttachment;
import com.intellectual.model.entity.PatentDisclosure;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 专利交底信息表（T表） 服务类接口
 *
 * @author 陈创
 * @since 2026-07-23 16:59
 */
public interface PatentDisclosureService extends IService<PatentDisclosure> {

    /** 在同一数据库事务中创建交底、附件元数据、缴费记录和开票记录。 */
    PatentDisclosure createWithAttachments(PatentDisclosureDTO request,
                                           List<MultipartFile> disclosureDocuments,
                                           List<MultipartFile> otherAttachments,
                                           Long sourceId,
                                           Long uploadUserId,
                                           String uploadUserName);

    /** 原子更换已有交底的 Word 交底书。 */
    DisclosureAttachment replaceDisclosureDocument(Long disclosureId,
                                                    String internalNo,
                                                    MultipartFile file,
                                                    Long uploadUserId,
                                                    String uploadUserName);

    /** 修改交底，并同步缴费表和开票表中的交底冗余信息。 */
    boolean updateWithRelatedRecords(PatentDisclosure disclosure);
}
