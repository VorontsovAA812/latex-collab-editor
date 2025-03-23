package com.example.demo.service.impl;

import com.example.demo.config.SecurityUtils;
import com.example.demo.domain.*;

import com.example.demo.repos.DocumentRepo;
import com.example.demo.repos.UserDocumentRepo;
import com.example.demo.rest.dto.DocumentDtos.NewDocumentRequest;
import com.example.demo.security.CustomUserDetails;
import com.example.demo.service.DocumentService;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class DocumentServiceImpl implements DocumentService {


    DocumentRepo documentRepo;
    UserService userService;
    UserDocumentRepo userDocumentRepo;
    private  final SecurityUtils securityUtils;

    @Autowired
    public DocumentServiceImpl(DocumentRepo documentRepo, UserService userService, UserDocumentRepo userDocumentRepo,SecurityUtils securityUtils) {
        this.documentRepo = documentRepo;
        this.userService = userService;
        this.userDocumentRepo = userDocumentRepo;
        this.securityUtils = securityUtils;

    }


    @Override
    public void addNewUserToDocument(Long userId, Long documentId) {
        // Найти документ по ID
        Optional<Document> element = documentRepo.findById(documentId);
        Document document = element.get();

        // Найти пользователя по ID
        User user = userService.findById(userId);

        // Установить связь пользователя с документом

        // Сохранить обновленного пользователя
        userService.save(user);
    }

    @Override
    public Long createDocument(NewDocumentRequest request, Authentication authentication) {


        Long userId = null;

        // Сначала проверяем обычную аутентификацию
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            userId = userDetails.getId();
        }
        // Если обычной аутентификации нет, пытаемся получить ID из SecurityUtils
        else {
            userId = securityUtils.getCurrentUserId();
        }





        User author = userService.findById(userId);


        // Создаем новый документ и устанавливаем заголовок и владельца
        Document document = new Document();
        document.setTitle(request.getTitle());
        document.setOwnerUser(author); // Обязательно устанавливаем владельца!

        // Сохраняем документ, чтобы получить его ID
        Document savedDocument = documentRepo.save(document);







        // Создаем запись в таблице user_documents для установления связи "пользователь-документ"
        UserDocument userDocument = new UserDocument();

        // Предполагается, что у вас есть класс-ключ UserDocumentId, состоящий из userId и documentId
        userDocument.setId(new UserDocumentId(author.getId(), savedDocument.getId()));
        userDocument.setUser(author);
        userDocument.setDocument(savedDocument);
        userDocument.setPermissionLevel("write");
        userDocument.setAddedAt(Instant.now());




        // Сохраняем связь через репозиторий user_documents
        userDocumentRepo.save(userDocument);


        return savedDocument.getId();


    }



}
