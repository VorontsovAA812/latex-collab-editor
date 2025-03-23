package com.example.demo.rest.controllers;

import com.example.demo.config.SecurityUtils;
import com.example.demo.rest.dto.DocumentDtos.NewDocumentRequest;
import com.example.demo.rest.dto.UserDtos.NewUserRequest;
import com.example.demo.security.CustomUserDetails;
import com.example.demo.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/api/v1/document")
public class DocumentController {
    private DocumentService documentService;
    private  final  SecurityUtils securityUtils;
    @Autowired
    public DocumentController(DocumentService documentService, SecurityUtils securityUtils) {
        this.documentService = documentService;
        this.securityUtils = securityUtils;
    }


    @PostMapping("/create")
    public ResponseEntity<Map<String, Long>> createDocument(@RequestBody NewDocumentRequest request, Authentication authentication) {

        // Передаем ID пользователя и данные документа в сервис
        Long documentId = documentService.createDocument(request, authentication);

        // Возвращаем JSON с ключом "documentId"
        return ResponseEntity.ok(Map.of("documentId", documentId));
    }

    @PostMapping("/{documentId}/user/{userId}")
    public ResponseEntity<Void> addNewUserToDocument(@PathVariable Long documentId, @PathVariable Long userId) {
        documentService.addNewUserToDocument(userId, documentId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
