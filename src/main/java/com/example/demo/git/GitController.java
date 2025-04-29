package com.example.demo.git;



import com.example.demo.rest.dto.DocumentDtos.DocumentResponse;
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
    public ResponseEntity<GitResponse> initRepo(@PathVariable Long documentId) throws GitAPIException, IOException {


        gitService.initRepo(documentId);
            return  ResponseEntity.ok((GitResponse.builder()
                    .message( "Git repository for document " + documentId + " created successfully")
                    .documentId(documentId).build()) );


    }

    @PostMapping("/commit")
    public ResponseEntity<GitResponse> commitDocument(@RequestBody LatexCommitRequest request) throws GitAPIException, IOException {

            gitService.commitDocument(request.getTexContent(), request.getDocumentId(), request.getAuthorName());
            return ResponseEntity.ok(GitResponse.builder()
                    .message("Document saved and committed successfully.")
                    .documentId(request.getDocumentId()).build());

    }

    @GetMapping("/history/{documentId}")
    public ResponseEntity<List<CommitInfo>> getCommitHistory(@PathVariable Long documentId) throws IOException, GitAPIException{

            List<CommitInfo> history = gitService.getCommitHistory(documentId);
            return ResponseEntity.ok(history);

    }



    @PostMapping("/{documentId}/restore")
    public ResponseEntity<GitResponse> restoreDocumentToCommit(@PathVariable Long documentId,@RequestBody RestoreToCommitRequest request) throws IOException, GitAPIException {

            gitService.restoreToCommit(documentId, request.getCommitId(), request.getUsername());

       return ResponseEntity.ok(GitResponse.builder()
                .message("Документ успешно откатан к версии " + request.getCommitId())
                .documentId(documentId).build());

    }








    }
