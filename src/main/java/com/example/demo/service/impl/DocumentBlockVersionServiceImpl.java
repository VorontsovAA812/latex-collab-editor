package com.example.demo.service.impl;


import com.example.demo.domain.DocumentBlock;
import com.example.demo.domain.DocumentBlockVersion;
import com.example.demo.domain.User;
import com.example.demo.repos.DocumentBlockRepo;
import com.example.demo.rest.dto.DocumentBlockVersionDtos.NewVersionRequest;
import com.example.demo.repos.DocumentBlockVersionRepo;
import com.example.demo.service.DocumentBlockVersionService;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class DocumentBlockVersionServiceImpl implements DocumentBlockVersionService {
    DocumentBlockVersionRepo  documentBlockVersionRepo;

    UserService userService;
    DocumentBlockRepo documentBlockRepo;


    @Autowired
    public DocumentBlockVersionServiceImpl(DocumentBlockVersionRepo documentBlockVersionRepo, UserService userService, DocumentBlockRepo documentBlockRepo) {
        this.documentBlockVersionRepo = documentBlockVersionRepo;
        this.userService=   userService;
        this.documentBlockRepo = documentBlockRepo;
    }


    public Long createNewVersion(NewVersionRequest request, Long userId,Long blockId)
    {
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
