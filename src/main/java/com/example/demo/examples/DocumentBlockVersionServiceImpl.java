/*
package com.example.demo.examples;


import com.example.demo.config.SecurityUtils;
import com.example.demo.domain.User;
import com.example.demo.rest.dto.DocumentBlockVersionDtos.NewVersionRequest;
import com.example.demo.security.CustomUserDetails;
import com.example.demo.service.DocumentBlockVersionService;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Deprecated
public class DocumentBlockVersionServiceImpl implements DocumentBlockVersionService {
    DocumentBlockVersionRepo  documentBlockVersionRepo;

    UserService userService;
    DocumentBlockRepo documentBlockRepo;
    SecurityUtils securityUtils;


    @Autowired
    public DocumentBlockVersionServiceImpl(DocumentBlockVersionRepo documentBlockVersionRepo, UserService userService, DocumentBlockRepo documentBlockRepo,SecurityUtils securityUtils ) {
        this.documentBlockVersionRepo = documentBlockVersionRepo;
        this.userService=   userService;
        this.documentBlockRepo = documentBlockRepo;
        this.securityUtils = securityUtils;
    }


    public Long createNewVersion(NewVersionRequest request, Authentication authentication , Long blockId)

    {

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


        User author =userService.findById(userId);
        DocumentBlockVersion documentBlockVersion = new DocumentBlockVersion();
        documentBlockVersion.setContent(request.getContent());
        documentBlockVersion.setAuthorUser(author);
        documentBlockVersion.setCreatedAt(Instant.now());

        Optional<DocumentBlock> element1 = documentBlockRepo.findById(blockId);
        DocumentBlock block  = element1.get();
        documentBlockVersion.setBlock(block);


        Optional<DocumentBlockVersion> element2 = documentBlockVersionRepo.findLastVersionByBlockId(blockId);
        DocumentBlockVersion lastVersion = element2.get();
        documentBlockVersion.setPreviousBlockVersion(lastVersion);
        documentBlockVersionRepo.save(documentBlockVersion);
        return  documentBlockVersion.getId();



    }








}

 */
