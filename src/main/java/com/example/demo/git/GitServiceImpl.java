package com.example.demo.git;


import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.springframework.stereotype.Service;
import org.eclipse.jgit.api.Git;

import java.io.File;
import java.io.FileOutputStream;
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
    public Git initRepo(Long documentId) throws GitAPIException, IOException {


        Path repoPath = Paths.get(sourcePath, documentId.toString()).toAbsolutePath().normalize();
        ; // /latex-versions/documentId


        // Если папка не существует — создаём
        if (Files.notExists(repoPath)) {
            Files.createDirectories(repoPath);
        }
        Git git = Git.init().setDirectory(repoPath.toFile()).call();

        System.out.println(repoPath.toFile());

        Path ignoreFile = repoPath.resolve(".gitignore"); // /latex-versions/documentId/gitignore


        Files.writeString(ignoreFile, "*.aux\n*.log\n*.out\n*.pdf\n*.toc\n*.synctex.gz\n");


        return git;

    }

    // метод который коммитит// зачем тогда в бд хранить контент
    public void saveAndCommit(String texContent, Long documentId, String authorName) throws GitAPIException, IOException {
        Path repoPath = Paths.get(sourcePath, documentId.toString()).toAbsolutePath().normalize();
        ; // /latex-versions/documentId
        Path textile = repoPath.resolve("main.tex");
        Files.writeString(textile, texContent);
        try (Git git = Git.open(repoPath.toFile())) {
            git.add().addFilepattern("main.tex").call();

            git.commit()
                    .setMessage("Document updated")
                    .setAuthor(authorName, authorName + "@editor.local")  // любой email для Git
                    .call();

        }

    }

    public List<CommitInfo> getHistory(Long documentId) throws IOException, GitAPIException {
        Path repoPath = Paths.get(sourcePath, documentId.toString()).toAbsolutePath().normalize();
        ; // /latex-versions/documentId

        List<CommitInfo> commits = new ArrayList<>(); // дописать!!!!!
        try
                (Git git = Git.open(repoPath.toFile())) {
            Iterable<RevCommit> infoAboutAllCommits = git.log().call();


            for (RevCommit commit : infoAboutAllCommits) {
                commits.add(
                        new CommitInfo(commit.getName(),
                                commit.getShortMessage(),
                                commit.getAuthorIdent().getName(),
                                Instant.ofEpochSecond(commit.getCommitTime())


                        ));

            }
        }


        return commits;

    }

    ;

    public void restoreToCommit(Long documentId, String commitId,String username) throws IOException,GitAPIException {
        File repoPath = new File(sourcePath, documentId.toString());

        try (Git git = Git.open(repoPath)) {
            Repository repository = git.getRepository(); // возвращаем объект для путешенствия по коммитам и деревьям

            ObjectId commitObjectId = repository.resolve(commitId); // из сокращенного айди, который есть в log получаем полный, с уоторым можно путешествовать по системе
            try (RevWalk revWalk = new RevWalk(repository)) {  // reyWalk позволяет путешестовать по коммитам
                RevCommit commit = revWalk.parseCommit(commitObjectId); // берем коммит с которым мы обратились
                RevTree tree = commit.getTree();  // Revtree позволяет ходить по всем файлам внутри коммита(в нашем случае получить доступ к файлу .tex
                // Ищем файл main.tex в этом коммите
                try (TreeWalk treeWalk = new TreeWalk(repository)) {
                    treeWalk.addTree(tree);
                    treeWalk.setRecursive(true);
                    treeWalk.setFilter(PathFilter.create("main.tex"));

                    if (!treeWalk.next()) {
                        throw new IllegalStateException("Файл main.tex не найден в коммите " + commitId);
                    }

                    ObjectId fileObjectId = treeWalk.getObjectId(0); //получить ID нужного файла (Blob).
                    ObjectLoader loader = repository.open(fileObjectId); //открыть файл через ObjectLoader

                    byte[] fileData = loader.getBytes();

                    // Перезаписываем текущий main.tex
                    File texFile = new File(repoPath, "main.tex");
                    try (FileOutputStream fos = new FileOutputStream(texFile)) {
                        fos.write(fileData);
                    }

                    // Делаем новый коммит
                    git.add().addFilepattern("main.tex").call();
                    git.commit()
                            .setMessage("Откат к версии " + commitId)
                            .setAuthor(username, username + "@editor.local")
                            .call();
                }

            }
        }


    }}
