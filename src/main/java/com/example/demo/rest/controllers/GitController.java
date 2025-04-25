package com.example.demo.rest.controllers;



import com.example.demo.service.impl.GitServiceImpl;
import lombok.RequiredArgsConstructor;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/git")
@RequiredArgsConstructor
public class GitController {

    private final GitServiceImpl gitService;

    @PostMapping("/init/{documentId}")
    public String initRepo(@PathVariable Long documentId) {
        try {
            gitService.initRepo(documentId);
            return "Git repository for document " + documentId + " created successfully.";
        } catch (GitAPIException | IOException e) {
            e.printStackTrace();
            return "Error creating repository: " + e.getMessage();
        }
    }
}
