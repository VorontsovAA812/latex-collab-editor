package com.example.demo.rest.controllers;


import com.example.demo.config.SecurityUtils;
import com.example.demo.rest.dto.DocumentBlockVersionDtos.NewVersionRequest;
import com.example.demo.security.CustomUserDetails;
import com.example.demo.service.DocumentBlockVersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/api/v1/blocks")
public class DocumentBlockVersionController {
    private final DocumentBlockVersionService service;

    @Autowired
    public DocumentBlockVersionController(DocumentBlockVersionService service) {
        this.service = service;
    }
    @ResponseBody
    @PostMapping("/{blockId}/versions")
    public ResponseEntity<Long> createNewVersion ((@PathVariable Long blockId, @RequestBody NewVersionRequest request, Authentication authentication )
    {

        Long versionId = service.createNewVersion(request, authentication, blockId);
        return ResponseEntity.status(HttpStatus.CREATED).body(versionId);



    }



}
