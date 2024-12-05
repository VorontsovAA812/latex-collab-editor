package com.example.demo.service.impl;

import com.example.demo.domain.Document;
import com.example.demo.domain.User;
import com.example.demo.repos.DocumentRepo;
import com.example.demo.rest.dto.DocumentDtos.NewDocumentRequest;
import com.example.demo.rest.dto.UserDtos.NewUserRequest;
import com.example.demo.rest.dto.UserDtos.UserDto;
import com.example.demo.security.CustomUserDetails;
import com.example.demo.service.DocumentService;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DocumentServiceImpl implements DocumentService {


    DocumentRepo documentRepo;
    UserService userService;

    @Autowired
    public DocumentServiceImpl(DocumentRepo documentRepo, UserService userService) {
        this.documentRepo = documentRepo;
        this.userService = userService;
    }


    @Override
    public void addNewUserToDocument(Long userId, Long documentId) {
        // Найти документ по ID
        Optional<Document> element = documentRepo.findById(documentId);
        Document document = element.get();

        // Найти пользователя по ID
        User user = userService.findById(userId);

        // Установить связь пользователя с документом
        user.setDocument(document);

        // Сохранить обновленного пользователя
        userService.save(user);
    }

    @Override
    public Long createDocument(NewDocumentRequest request, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getId();
        User author = userService.findById(userId);

        Document document = new Document();
        document.setTitle(request.getTitle());
        document.setContent(request.getContent());
        document.setAuthor(author);
        documentRepo.saveAndFlush(document);


        author.setDocument(document);
        userService.save(author); // Сохранение пользователя с обновленным document_id



        return document.getId();

    }



}
