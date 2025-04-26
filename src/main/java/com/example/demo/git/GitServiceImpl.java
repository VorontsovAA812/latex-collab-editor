package com.example.demo.git;


import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.springframework.stereotype.Service;
import org.eclipse.jgit.api.Git;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;


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

    // метод который коммитит// зачем тогда в бд хранить контент
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

    public List<CommitInfo> getHistory(Long documentId)throws IOException, GitAPIException
    {
        Path repoPath = Paths.get(sourcePath, documentId.toString()).toAbsolutePath().normalize();; // /latex-versions/documentId

        Git git = Git.open(repoPath.toFile());
        Iterable<RevCommit> infoAboutAllCommits = git.log().call();

        List<CommitInfo> commits = new ArrayList<>(); // дописать!!!!!

        for (RevCommit commit : infoAboutAllCommits)
        {
            commits.add(
                    new CommitInfo(commit.getName(),
                            commit.getShortMessage(),
                            commit.getAuthorIdent().getName(),
                            Instant.ofEpochSecond(commit.getCommitTime())


                    ));

        }
        return commits;







    };





}
