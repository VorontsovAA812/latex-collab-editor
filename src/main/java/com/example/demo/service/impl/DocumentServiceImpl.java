package com.example.demo.service.impl;

import com.example.demo.config.SecurityUtils;
import com.example.demo.domain.*;

import com.example.demo.exception.BusinessException;
import com.example.demo.git.GitService;
import com.example.demo.repos.DocumentRepo;
import com.example.demo.repos.UserDocumentRepo;
import com.example.demo.rest.dto.DocumentDtos.ContentRequestDto;
import com.example.demo.rest.dto.DocumentDtos.DocumentListDTO;
import com.example.demo.rest.dto.DocumentDtos.DocumentResponse;
import com.example.demo.rest.dto.DocumentDtos.NewDocumentRequest;
import com.example.demo.security.CustomUserDetails;
import com.example.demo.service.DocumentService;
import com.example.demo.service.UserService;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import javax.print.Doc;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class DocumentServiceImpl implements DocumentService {


    DocumentRepo documentRepo;
    UserService userService;
    UserDocumentRepo userDocumentRepo;
    GitService gitService;
    private  final SecurityUtils securityUtils;

    @Autowired
    public DocumentServiceImpl(DocumentRepo documentRepo, UserService userService, UserDocumentRepo userDocumentRepo,SecurityUtils securityUtils, GitService gitService ) {
        this.documentRepo = documentRepo;
        this.userService = userService;
        this.userDocumentRepo = userDocumentRepo;
        this.securityUtils = securityUtils;
        this.gitService=gitService;

    }

    public  Long  getCurrentUserId(Authentication authentication) {
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
        return userId;
    }

    public DocumentResponse convertDocumentToResponse(Document document,  boolean includeContent) throws IOException {
        DocumentResponse.DocumentResponseBuilder builder= DocumentResponse.builder()
                .id(document.getId())
                .title(document.getTitle())
                .ownerUsername(document.getOwnerUser().getUsername())
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt());

         if(includeContent) {
              builder.content(gitService.getLastVersionContent(document.getId()));
         }
         return builder.build();
    }

    @Transactional
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


    @Transactional(readOnly = true)
    @Override
    public DocumentResponse findById(Long id,boolean includeContent) throws IOException {

        Optional<Document> element = documentRepo.findById(id);
        if (element.isPresent()) {

            Document document =  element.get();

            String content = null;
            if(includeContent){
                content = gitService.getLastVersionContent(id);

            }

            return DocumentResponse.builder()
                    .id(document.getId())
                    .title(document.getTitle())
                    .ownerUsername(document.getOwnerUser().getUsername())
                    .createdAt(document.getCreatedAt())
                    .updatedAt(document.getUpdatedAt())
                    .content(content)
                    .build();
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Документ не найден");
        }
    }


    @Transactional(readOnly = true)
    @Override
    public List<DocumentListDTO> getDocumentsForCurrentUser(Authentication authentication) {


        Long userId = getCurrentUserId(authentication);

        List<Document> documents =documentRepo.findAllDocumentsByUserId(userId);
        List<DocumentListDTO> result = new ArrayList<>();
        for (Document doc : documents) {
            DocumentListDTO dto = new DocumentListDTO(doc.getTitle(),doc.getId());
            result.add(dto);
        }

        return  result;

    }


    @Transactional
    @Override
    public Long createDocument(NewDocumentRequest request, Authentication authentication) throws GitAPIException, IOException {


        Long userId = getCurrentUserId(authentication);






        User author = userService.findById(userId);


        // Создаем новый документ и устанавливаем заголовок и владельца
        Document document = new Document();
        document.setTitle(request.getTitle());
        document.setOwnerUser(author); // Обязательно устанавливаем владельца!
        // Явно устанавливаем timestamp
        document.setCreatedAt(Instant.now());
        document.setUpdatedAt(Instant.now());
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

        gitService.initializeGitWithFirstCommit(savedDocument.getId(), request.getContent(), author.getUsername());


        return savedDocument.getId();


    }

    // метод для сохранения документа:
    @Transactional
    @Override
    public DocumentResponse updateDocument(Long id, NewDocumentRequest updateDocumentRequest,  Authentication authentication) throws GitAPIException, IOException {
        Long userId = getCurrentUserId(authentication);


        Optional<Document> element = documentRepo.findById(id);
        Document document = element.get();

        document.setTitle(updateDocumentRequest.getTitle());
        document.setUpdatedAt(Instant.now());

        documentRepo.save(document);

        User  user2= userService.findById(userId);
        gitService.commitDocument(updateDocumentRequest.getContent(),id,user2.getUsername());
        return convertDocumentToResponse(document,true);

    }

    public void deleteDocument(Long documentId, Authentication authentication) throws IOException {

        Long userId = getCurrentUserId(authentication);

        Optional<Document> element = documentRepo.findById(documentId);

        Document document = element.get();

        if(!document.getOwnerUser().getId().equals(userId))
        {
            throw new BusinessException("Вы не можете удалить этот документ");
        }

        Path repoPath = Paths.get("./latex-versions", documentId.toString()).toAbsolutePath().normalize();

        FileUtils.deleteDirectory(repoPath.toFile());

        Path repoPath2 = Paths.get("./latex-files", documentId.toString()).toAbsolutePath().normalize();
        FileUtils.deleteDirectory(repoPath2.toFile());

        documentRepo.delete(document);




    }






}
