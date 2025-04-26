package com.example.demo.rest.controllers;



import com.example.demo.service.impl.GitServiceImpl;
import lombok.RequiredArgsConstructor;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("/commit")
    public ResponseEntity<String> saveAndCommit(@RequestBody LatexCommitRequest request) {
        try {
            gitService.saveAndCommit(request.getTexContent(), request.getDocumentId(), request.getAuthorName());
            return ResponseEntity.ok("Document saved and committed successfully.");
        } catch (GitAPIException | IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error during save and commit: " + e.getMessage());
        }
    }

}
