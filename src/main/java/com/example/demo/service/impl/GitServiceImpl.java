package com.example.demo.service.impl;


import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.stereotype.Service;
import org.eclipse.jgit.api.Git;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


@Service
public class GitServiceImpl {

    private final String sourcePath = "./latex-versions";  // здесь git, main.tex


    public Git initRepo(Long documentId) throws GitAPIException,IOException {


        Path repoPath = Paths.get(sourcePath, documentId.toString()).toAbsolutePath().normalize();; // /latex-versions/documentId


        // Если папка не существует — создаём
        if (Files.notExists(repoPath)) {
            Files.createDirectories(repoPath);
        }
        Git git = Git.init().setDirectory(repoPath.toFile()).call();

        System.out.println(repoPath.toFile());

        Path ignoreFile = repoPath.resolve(".gitignore"); // /latex-versions/documentId/gitignore



        Files.writeString(ignoreFile,"*.aux\n*.log\n*.out\n*.pdf\n*.toc\n*.synctex.gz\n");


        return git;







    }
}
