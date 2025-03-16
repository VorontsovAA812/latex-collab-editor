package com.example.demo.service.impl;

import com.example.demo.domain.*;
import com.example.demo.repos.DocumentBlockRepo;
import com.example.demo.repos.DocumentBlockVersionRepo;
import com.example.demo.repos.DocumentRepo;
import com.example.demo.repos.UserDocumentRepo;
import com.example.demo.rest.dto.DocumentDtos.NewDocumentRequest;
import com.example.demo.rest.dto.UserDtos.NewUserRequest;
import com.example.demo.rest.dto.UserDtos.UserDto;
import com.example.demo.security.CustomUserDetails;
import com.example.demo.service.DocumentService;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class DocumentServiceImpl implements DocumentService {


    private final DocumentBlockRepo documentBlockRepo;
    DocumentRepo documentRepo;
    UserService userService;
    UserDocumentRepo userDocumentRepo;
    DocumentBlockVersionRepo documentBlockVersionRepo;
    @Autowired
    public DocumentServiceImpl(DocumentRepo documentRepo, UserService userService, UserDocumentRepo userDocumentRepo, DocumentBlockRepo documentBlockRepo, DocumentBlockVersionRepo documentBlockVersionRepo) {
        this.documentRepo = documentRepo;
        this.userService = userService;
        this.userDocumentRepo = userDocumentRepo;
        this.documentBlockRepo = documentBlockRepo;
        this.documentBlockVersionRepo = documentBlockVersionRepo;
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
    public Long createDocument(NewDocumentRequest request, Long userId) {
        User author = userService.findById(userId);

        // Создаем новый документ и устанавливаем заголовок и владельца
        Document document = new Document();
        document.setTitle(request.getTitle());
        document.setOwnerUser(author); // Обязательно устанавливаем владельца!

        // Сохраняем документ, чтобы получить его ID
        Document savedDocument = documentRepo.save(document);
        DocumentBlock documentBlock = new DocumentBlock();
        documentBlock.setDocument(savedDocument);
        documentBlock.setTitle("Основное содержание"); // Можно изменить заголовок
        documentBlock.setOrderIndex(0); // Первый блок
        documentBlock.setCreatedAt((Instant.now()));
        DocumentBlock savedBlock = documentBlockRepo.save(documentBlock);

        DocumentBlockVersion blockVersion = new DocumentBlockVersion();
        blockVersion.setBlock(savedBlock);
        blockVersion.setAuthorUser(author);
        blockVersion.setContent(request.getContent()); // Начальное содержимое из запроса
        blockVersion.setCreatedAt(Instant.now());  // устанавливаем время регистрации

        documentBlockVersionRepo.save(blockVersion);







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
