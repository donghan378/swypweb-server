package com.swyp14.phocamatch.legal.dto;

import com.swyp14.phocamatch.legal.domain.LegalDocument;
import com.swyp14.phocamatch.legal.repository.LegalDocumentRepository;

import java.time.LocalDateTime;

public record LegalDocumentResponse(
        String content,
        LocalDateTime updatedAt
) {

    public static LegalDocumentResponse from(
            LegalDocument legalDocument
    ){
        return new LegalDocumentResponse(
                legalDocument.getContent(),
                legalDocument.getUpdatedAt()
        );
    }
}
