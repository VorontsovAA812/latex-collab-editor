package com.example.demo.service.impl;


import org.springframework.stereotype.Service;
import org.eclipse.jgit.api.Git;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


@Service
public class GitServiceImpl {

    private final String sourcePath = "latex-versions";  // здесь git, main.tex


    public Git initRepo(Long documentId)
    {
        Path repoPath = Paths.get(sourcePath,documentId.toString()); // /latex-versions/documentId
        // Если папка не существует — создаём
        if (Files.notExists(repoPath)) {
            Files.createDirectories(repoPath);
        }



    }
}
