package com.example.demo.git;



import lombok.RequiredArgsConstructor;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

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
    public ResponseEntity<String> commitDocument(@RequestBody LatexCommitRequest request) {
        try {
            gitService.saveAndCommit(request.getTexContent(), request.getDocumentId(), request.getAuthorName());
            return ResponseEntity.ok("Document saved and committed successfully.");
        } catch (GitAPIException | IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error during save and commit: " + e.getMessage());
        }
    }

    @GetMapping("/history/{documentId}")
    public ResponseEntity<List<CommitInfo>> getCommitHistory(@PathVariable Long documentId) {
        try {
            List<CommitInfo> history = gitService.getHistory(documentId);
            return ResponseEntity.ok(history);
        } catch (IOException | GitAPIException e) {
            return ResponseEntity.internalServerError().build();
        }
    }





    @PostMapping("/{documentId}/restore")
    public ResponseEntity<String> restoreDocumentToCommit(
            @PathVariable Long documentId,
            @RequestParam String commitId,
            @RequestParam String username) {
        try {
            gitService.restoreToCommit(documentId, commitId, username);
            return ResponseEntity.ok("Документ успешно откатан к версии " + commitId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Неизвестная ошибка при откате: " + e.getMessage());
        }
    }





@PostMapping("/{documentId}/restore")
public ResponseEntity<String> restoreDocumentToCommit(
        @RequestBody  restoreToCommitRequest request) {
    try {
        gitService.restoreToCommit(request.documentId, request.commitId, request.username);
        return ResponseEntity.ok("Документ успешно откатан к версии " + request.commitId);
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Неизвестная ошибка при откате: " + e.getMessage());
    }
}



    }
