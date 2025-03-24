package com.example.demo.service;

import com.example.demo.domain.Document;
import com.example.demo.domain.User;
import com.example.demo.rest.dto.DocumentDtos.DocumentListDTO;
import com.example.demo.rest.dto.DocumentDtos.NewDocumentRequest;
import org.springframework.security.core.Authentication;

import javax.print.Doc;
import java.util.List;

public interface DocumentService {
    Long createDocument(NewDocumentRequest request, Authentication authentication);
    void addNewUserToDocument(Long userId, Long documentId);
    List<DocumentListDTO> getDocumentsForCurrentUser(Authentication authentication);
}
