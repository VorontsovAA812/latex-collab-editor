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

// создаение и иницализирование репоитория под нужный документ
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

    // метод который коммитит
    public void saveAndCommit( String texContent, Long documentId,String authorName) throws GitAPIException,IOException
    {
        Path repoPath = Paths.get(sourcePath, documentId.toString()).toAbsolutePath().normalize();; // /latex-versions/documentId
        Path textile=repoPath.resolve("main.tex");
        Files.writeString(textile,texContent);
        Git git = Git.open(repoPath.toFile());
        git.add().addFilepattern("main.tex").call();

        git.commit()
                .setMessage("Document updated")
                .setAuthor(authorName, authorName + "@editor.local")  // любой email для Git
                .call();

    }
}
