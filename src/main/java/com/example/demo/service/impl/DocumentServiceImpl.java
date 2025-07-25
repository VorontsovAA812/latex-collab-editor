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
import com.example.demo.rest.dto.UserDtos.UserDto;
import com.example.demo.security.CustomUserDetails;
import com.example.demo.service.DocumentService;
import com.example.demo.service.UserService;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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

    private static final Logger log = LoggerFactory.getLogger(DocumentServiceImpl.class);


    DocumentRepo documentRepo;

    UserService userService;
    UserDocumentRepo userDocumentRepo;
    GitService gitService;
    private  final SecurityUtils securityUtils;

    @Autowired
    public DocumentServiceImpl(DocumentRepo documentRepo, UserService userService, UserDocumentRepo userDocumentRepo,SecurityUtils securityUtils, @Lazy GitService gitService ) {
        this.documentRepo = documentRepo;
        this.userService = userService;
        this.userDocumentRepo = userDocumentRepo;
        this.securityUtils = securityUtils;
        this.gitService=gitService;

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

    @Override
    public Document findById(Long id)  {

        Optional<Document> element = documentRepo.findById(id);
        if (element.isPresent()) {

            Document document = element.get();
            return document;
        }

        else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Документ не найден");
        }
    }


    @Transactional(readOnly = true)
    @Override
    public List<DocumentListDTO> getDocumentsForCurrentUser(Authentication authentication) {


        Long userId = userService.getCurrentUserId(authentication);

        List<Document> documents =documentRepo.findAllDocumentsByUserId(userId);
        List<DocumentListDTO> result = new ArrayList<>();
        for (Document doc : documents) {
            DocumentListDTO dto;
            if(userId.equals(doc.getOwnerUser().getId())) {
                dto = new DocumentListDTO(doc.getTitle(), doc.getId(), doc.getOwnerUser().getUsername(),true);
                log.info("первый случай");

            }
            else{
               dto = new DocumentListDTO(doc.getTitle(), doc.getId(), doc.getOwnerUser().getUsername(),false);

            }

            log.info("Current userId: {}", userId);
            log.info("Doc ownerId: {}", doc.getOwnerUser().getId());



            result.add(dto);
        }

        return  result;

    }


    @Transactional
    @Override
    public Long createDocument(NewDocumentRequest request, Authentication authentication) throws GitAPIException, IOException {


        Long userId = userService.getCurrentUserId(authentication);






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
        Long userId = userService.getCurrentUserId(authentication);

        Document document = documentRepo.findById(id)
                .orElseThrow(() -> new BusinessException("Документ не найден"));
        boolean isAuthor = userId.equals(document.getOwnerUser().getId());

        // Запрет для НЕ автора редактировать до появления main
        if (!isAuthor && !gitService.hasMainBranch(id)) {
            throw new BusinessException("Нельзя редактировать документ до утверждения автором.");
        }
        User  user2= userService.findById(userId);


        document.setTitle(updateDocumentRequest.getTitle());
        document.setUpdatedAt(Instant.now());

        documentRepo.save(document);

        try {
            gitService.commitToUserBranch(updateDocumentRequest.getContent(), id, user2.getUsername());
        } catch (Exception ex) {
            log.warn("Не удалось закоммитить изменения в Git для документа {}: {}", id, ex.getMessage());
        }

        return convertDocumentToResponse(document,true);

    }

    @Transactional
    @Override
    public Long deleteDocument(Long documentId, Authentication authentication) throws IOException {

        Long userId = userService.getCurrentUserId(authentication);

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

        return documentId;




    }

    @Transactional
    @Override
   public UserDto inviteUserToDocument(Long documentId, String username, Authentication authentication) {

        // 1. Проверка: существует ли документ
        Document document = documentRepo.findById(documentId)
                .orElseThrow(() -> new BusinessException("Документ не найден"));


        Long userId = userService.getCurrentUserId(authentication);
        if(!userId.equals(document.getOwnerUser().getId())) {
            throw  new BusinessException("Только автор может приглашать пользователей");


        }


        User userToInvite = userService.findByUsername(username);
        /*
        if(!userToInvite.getIsOnline())
        {
            throw new BusinessException("Пользователь не онлайн");
        }
        */

        UserDocumentId id = new UserDocumentId(userToInvite.getId(), documentId);
        if (userDocumentRepo.existsById(id)) {
            throw new BusinessException("Пользователь уже имеет доступ к документу");
        }
        UserDocument userDocument = new UserDocument();
        userDocument.setId(id);


        userDocument.setUser(userToInvite);
        userDocument.setDocument(document);
        userDocument.setAddedAt(Instant.now());
        userDocument.setPermissionLevel("write");
        userDocumentRepo.save(userDocument);


        return new UserDto(userToInvite.getUsername(),userToInvite.getRole(),userToInvite.getIsOnline());


        }
@Transactional
@Override
    public Long leaveDocument(Long documentId, Authentication authentication) {
        Long userId = userService.getCurrentUserId(authentication);

        UserDocumentId id = new UserDocumentId(userId, documentId);

        Document document = documentRepo.findById(documentId)
                .orElseThrow(() -> new BusinessException("Документ не найден"));

        if (userId.equals(document.getOwnerUser().getId())) {
            throw new BusinessException("Автор не может локально удалить документ");
        }

        if (!userDocumentRepo.existsById(id)) {
            throw new BusinessException("Вы не являетесь участником документа");
        }

        userDocumentRepo.deleteById(id);
        return  documentId;

    }




    @Transactional
    @Override
    public void deleteAllDocuments() throws IOException {
        List<Document> allDocuments = documentRepo.findAll();

        for (Document doc : allDocuments) {
            Long documentId = doc.getId();

            // Удаляем Git-репозиторий
            Path gitPath = Paths.get("./latex-versions", documentId.toString()).toAbsolutePath();
            FileUtils.deleteDirectory(gitPath.toFile());

            // Удаляем latex-файлы
            Path latexPath = Paths.get("./latex-files", documentId.toString()).toAbsolutePath();
            FileUtils.deleteDirectory(latexPath.toFile());
        }

        // Удаление всех записей
        documentRepo.deleteAll();
    }



}








