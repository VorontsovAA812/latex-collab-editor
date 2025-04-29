package com.example.demo.git;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;


public class restoreToCommitRequest {


    Long documentId;
    String commitId;
    String username;
}
