package com.example.demo.rest.controllers;

import com.example.demo.config.SecurityUtils;
import com.example.demo.rest.dto.DocumentDtos.ContentRequestDto;
import com.example.demo.rest.dto.DocumentDtos.DocumentListDTO;
import com.example.demo.rest.dto.DocumentDtos.DocumentResponse;
import com.example.demo.rest.dto.DocumentDtos.NewDocumentRequest;
import com.example.demo.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/api/v1/documents")
public class DocumentController {
    private final DocumentService documentService;
    private  final  SecurityUtils securityUtils;
    @Autowired
    public DocumentController(DocumentService documentService, SecurityUtils securityUtils) {
        this.documentService = documentService;
        this.securityUtils = securityUtils;
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponse> findById(@PathVariable Long id) {
        return  ResponseEntity.ok(documentService.findById(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<DocumentResponse> updateDocument(@PathVariable Long id, @RequestBody NewDocumentRequest updateDocumentRequest) {

        return  ResponseEntity.ok(documentService.updateDocument(id,updateDocumentRequest));
    }


    @PostMapping("")
    public ResponseEntity<Map<String, Long>> createDocument(@RequestBody NewDocumentRequest request, Authentication authentication) {

        // Передаем ID пользователя и данные документа в сервис
        Long documentId = documentService.createDocument(request, authentication);

        // Возвращаем JSON с ключом "documentId"
        return ResponseEntity.ok(Map.of("documentId", documentId));
    }


    @GetMapping("/documentList")
    public ResponseEntity<List<DocumentListDTO>> getDocumentsForCurrentUser(Authentication authentication) {
        List<DocumentListDTO> documents = documentService.getDocumentsForCurrentUser( authentication);



        return ResponseEntity.ok( documents);
    }


    @PostMapping("/{documentId}/user/{userId}")
    public ResponseEntity<Void> addNewUserToDocument(@PathVariable Long documentId, @PathVariable Long userId) {
        documentService.addNewUserToDocument(userId, documentId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
