package com.swyp14.phocamatch.legal.service;

import com.swyp14.phocamatch.legal.domain.LegalDocument;
import com.swyp14.phocamatch.legal.domain.LegalDocumentType;
import com.swyp14.phocamatch.legal.dto.LegalDocumentResponse;
import com.swyp14.phocamatch.legal.exception.LegalDocumentNotFoundException;
import com.swyp14.phocamatch.legal.repository.LegalDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LegalDocumentService {

    private final LegalDocumentRepository legalDocumentRepository;

    @Transactional(readOnly = true)
    public LegalDocumentResponse getTermsOfService() {
        LegalDocument legalDocument =
                legalDocumentRepository
                        .findByDocumentType(
                                LegalDocumentType.TERMS_OF_SERVICE
                        )
                        .orElseThrow(
                                LegalDocumentNotFoundException::new
                        );

        return LegalDocumentResponse.from(legalDocument);
    }

    @Transactional(readOnly = true)
    public LegalDocumentResponse getPrivacyPolicy() {
        LegalDocument legalDocument =
                legalDocumentRepository
                        .findByDocumentType(
                                LegalDocumentType.PRIVACY_POLICY
                        )
                        .orElseThrow(
                                LegalDocumentNotFoundException::new
                        );

        return LegalDocumentResponse.from(legalDocument);
    }
}
