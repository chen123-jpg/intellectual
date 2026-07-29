package com.intellectual.model.constants;

import java.util.Set;

public class TtableConstant {

    public static final String ROLE_ADMIN = "admin";
    public static final String ROLE_PROJECT_INITIATOR = "projectInitiator";
    public static final String ROLE_ORGANIZER = "organizer";
    public static final String ROLE_PROCESS_OPERATOR = "processOperator";
    public static final long ROLE_ID_PROCESS_OPERATOR = 7L;
    public static final String DISCLOSURE_STATUS_FINAL = "定稿";
    public static final String DISCLOSURE_STATUS_PENDING_REPORT = "定稿待报";
    public static final String DISCLOSURE_STATUS_REPORTED = "已申报";

    public static final String PACKAGE_STATUS_DRAFT = "DRAFT";
    public static final String PACKAGE_STATUS_PENDING_RECEIVE = "PENDING_RECEIVE";
    public static final String PACKAGE_STATUS_REVIEWING = "REVIEWING";
    public static final String PACKAGE_STATUS_REJECTED = "REJECTED";
    public static final String PACKAGE_STATUS_APPROVED = "APPROVED";
    public static final String PACKAGE_STATUS_SUBMITTED = "SUBMITTED";

    public static final String PACKAGE_FILE_ROLE_DOCUMENT = "PACKAGE_DOCUMENT";
    public static final String PACKAGE_FILE_ROLE_RECEIPT = "CNIPA_RECEIPT";
    public static final String PACKAGE_DOCUMENT_XML = "XML";
    public static final String PACKAGE_DOCUMENT_REQUEST = "REQUEST";
    public static final String PACKAGE_DOCUMENT_DESCRIPTION = "DESCRIPTION";
    public static final String PACKAGE_DOCUMENT_CLAIMS = "CLAIMS";
    public static final String PACKAGE_DOCUMENT_ABSTRACT = "ABSTRACT";
    public static final String PACKAGE_DOCUMENT_ABSTRACT_DRAWING = "ABSTRACT_DRAWING";
    public static final String PACKAGE_DOCUMENT_RECEIPT = "CNIPA_RECEIPT";
    public static final Set<String> PACKAGE_DOCUMENT_CODES = Set.of(
            PACKAGE_DOCUMENT_XML,
            PACKAGE_DOCUMENT_REQUEST,
            PACKAGE_DOCUMENT_DESCRIPTION,
            PACKAGE_DOCUMENT_CLAIMS,
            PACKAGE_DOCUMENT_ABSTRACT,
            PACKAGE_DOCUMENT_ABSTRACT_DRAWING
    );
    public static final String APPLICATION_PACKAGE_XML = "XML_PACKAGE";
    public static final String APPLICATION_PACKAGE_FIVE_BOOKS = "FIVE_BOOKS_WORD";
    public static final String APPLICATION_PACKAGE_UNCONFIRMED = "UNCONFIRMED";
    public static final String APPLICATION_PACKAGE_CONFIRMED = "CONFIRMED";
    public static final Set<String> APPLICATION_PACKAGE_TYPES =
            Set.of(APPLICATION_PACKAGE_XML, APPLICATION_PACKAGE_FIVE_BOOKS);
    public static final String DISCLOSURE_DOC = "DISCLOSURE_DOC";
    public static final String DISCLOSURE_OTHER = "DISCLOSURE_OTHER";
    public static final Set<String> DISCLOSURE_ATTACHMENT_TYPES =
            Set.of(DISCLOSURE_DOC, DISCLOSURE_OTHER);
    public static final Set<String> WORD_EXTENSIONS = Set.of("doc", "docx");
}
