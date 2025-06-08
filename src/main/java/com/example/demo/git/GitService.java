package com.example.demo.git;


import com.example.demo.domain.Document;
import com.example.demo.domain.User;
import com.example.demo.domain.UserDocument;
import com.example.demo.domain.UserDocumentId;
import com.example.demo.repos.UserDocumentRepo;
import com.example.demo.service.DocumentService;
import com.example.demo.service.UserService;
import com.example.demo.service.impl.UserServiceImpl;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
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
import java.util.Set;
import java.util.stream.Collectors;

import static java.nio.file.Files.readString;
import static java.util.Locale.filter;


@Service
public class GitService {

    private final String sourcePath = "./latex-versions";  // здесь git, main.tex

    private final UserService userService;
    private final UserDocumentRepo userDocumentRepository;

    private final  DocumentService documentService;
    @Autowired
    public GitService(UserService userService,UserDocumentRepo userDocumentRepository,DocumentService documentService) {
        this.userService = userService;
        this.userDocumentRepository=userDocumentRepository;
        this.documentService=documentService;
    }


    public void initializeGitWithFirstCommit(Long documentId, String content, String authorName) throws GitAPIException, IOException {
        Path repoPath = Paths.get(sourcePath, documentId.toString()).toAbsolutePath().normalize();
        Path mainTexPath = repoPath.resolve("main.tex");
        String userBranch = "user-" + authorName;

        try (Git git = initRepo(documentId)) {
            // Пишем содержимое сразу
            Files.writeString(mainTexPath, content);
            git.add().addFilepattern("main.tex").call();

            // Первый коммит от пользователя
            RevCommit commit = git.commit()
                    .setMessage("Первый коммит от автора")
                    .setAuthor(authorName, authorName + "@editor.local")
                    .call();

            // Создаём ветку пользователя из этого коммита
            git.branchCreate()
                    .setName(userBranch)
                    .setStartPoint(commit)
                    .call();

            // Переключаемся в ветку пользователя
            git.checkout().setName(userBranch).call();
        }
    }
    public boolean hasMainBranch(Long documentId) throws IOException {
        Path repoPath = Paths.get(sourcePath, documentId.toString()).toAbsolutePath().normalize();
        try (Git git = Git.open(repoPath.toFile())) {
            if(git.getRepository().findRef("main")!=null){
                return true;
            }
            else{
                return false;
            }

        }

    }
    public String createInitialUserBranch(Long documentId, String username) throws IOException, GitAPIException {
        Path repoPath = Paths.get(sourcePath, documentId.toString()).toAbsolutePath().normalize();
        String branchName = "user-" + username;

        try (Git git = Git.open(repoPath.toFile())) {
            if (git.getRepository().findRef(branchName) == null) {
                // Ветка не существует — создаём
                git.checkout()
                        .setCreateBranch(true)
                        .setStartPoint("HEAD")
                        .setName(branchName)
                        .call();
            } else {
                // Ветка уже есть — просто переключаемся
                git.checkout()
                        .setName(branchName)
                        .call();
            }
            return branchName;
        }
    }

    public boolean createMainFromUserBranch(Long documentId,Authentication authentication) throws IOException, GitAPIException {
        Long userId = userService.getCurrentUserId(authentication);
        String authorName= userService.findById(userId).getUsername();
        Path repoPath = Paths.get(sourcePath, documentId.toString()).toAbsolutePath().normalize();
        String userBranch = "user-" + authorName;
        String mainBranch = "main";

        try (Git git = Git.open(repoPath.toFile())) {
            // Проверка: существует ли уже main
            if (git.getRepository().findRef(mainBranch) != null) {
                throw new IllegalStateException("main уже существует. Утверждение уже было.");
            }

            // Убедимся, что пользовательская ветка есть
            if (git.getRepository().findRef(userBranch) == null) {
                throw new RefNotFoundException("Ветка " + userBranch + " не найдена.");
            }

            // Создаём ветку main из userBranch
            git.checkout().setName(userBranch).call();
            git.branchCreate().setName(mainBranch).setStartPoint(userBranch).call();

            // Возвращаемся в main
            git.checkout().setName(mainBranch).call();

            return true;
        }
    }





    public String getLastVersionContent(Long documentId) throws IOException {
        Path repoPath = Paths.get(sourcePath, documentId.toString()).toAbsolutePath().normalize();
        Path textile = repoPath.resolve("main.tex");

        // Если main.tex не сущ
        if (Files.notExists(textile)) {
            throw new FileNotFoundException("Вы ничего не сохранили ( ни одного коммита пока что нет)");
        }

        return Files.readString(textile);


    }

    // создаение и иницализирование репоитория под нужный документ
    public Git initRepo(Long documentId) throws GitAPIException, IOException {


        Path repoPath = Paths.get(sourcePath, documentId.toString()).toAbsolutePath().normalize();
        ; // /latex-versions/documentId


        // Если папка не существует — создаём
        if (Files.notExists(repoPath)) {
            Files.createDirectories(repoPath);
        }

        Path gitPath = repoPath.resolve(".git");

        if (Files.exists(gitPath)) {
            // Репозиторий уже инициализирован
            return Git.open(repoPath.toFile());
        }


        Git git = Git.init().setDirectory(repoPath.toFile()).call();

        System.out.println(repoPath.toFile());

        Path ignoreFile = repoPath.resolve(".gitignore"); // /latex-versions/documentId/gitignore


        Files.writeString(ignoreFile, "*.aux\n*.log\n*.out\n*.pdf\n*.toc\n*.synctex.gz\n");


        return git;

    }

    // метод который коммитит// зачем тогда в бд хранить контент
    public CommitInfo commitDocument(String texContent, Long documentId, String authorName) throws GitAPIException, IOException {

        RevCommit commit;
        Path repoPath = Paths.get(sourcePath, documentId.toString()).toAbsolutePath().normalize();
        ; // /latex-versions/documentId

        Path textile = repoPath.resolve("main.tex");
        Files.writeString(textile, texContent);
        try (Git git = Git.open(repoPath.toFile())) {
            git.add().addFilepattern("main.tex").call();

            commit = git.commit()
                    .setMessage("Внесены изменения")
                    .setAuthor(authorName, authorName + "@editor.local")  // любой email для Git
                    .call();

        }
        return convertCommitToInfo(commit);
    }


    public List<CommitInfo> getCommitHistory(Long documentId) throws IOException, GitAPIException {
        Path repoPath = Paths.get(sourcePath, documentId.toString()).toAbsolutePath().normalize();
        List<CommitInfo> commits = new ArrayList<>();

        try (Git git = Git.open(repoPath.toFile())) {
            try (RevWalk revWalk = new RevWalk(git.getRepository())) {
                for (RevCommit commit : git.log().call()) {
                    revWalk.parseBody(commit); //
                    commits.add(convertCommitToInfo(commit));
                }
            }
        }
        return commits;
    }

    public RevCommit getCurrentCommit(Long documentId) throws IOException, GitAPIException {
        File repoDir = new File(sourcePath, documentId.toString()); //  открываем директорию документа
        try (Git git = Git.open(repoDir)) {
            Repository repository = git.getRepository(); // возвращаем объект для путешенствия по коммитам и деревьям
            ObjectId headId = repository.resolve("HEAD");
            try (RevWalk revWalk = new RevWalk(git.getRepository())) {
                RevCommit commit = revWalk.parseCommit(headId);
                revWalk.parseBody(commit);

                return commit;
            }
        }
    }


    public CommitInfo getPreviousCommit(Long documentId) throws GitAPIException, IOException {


        RevCommit currentCommit = getCurrentCommit(documentId);


        if (currentCommit.getParentCount() == 0) {
            throw new NoPreviousCommitException("Документ нельзя откатить — это первая версия");
        }


        RevCommit previousCommit;
        try (Git git = Git.open(new File(sourcePath, documentId.toString()));
             RevWalk revWalk = new RevWalk(git.getRepository())) {

            previousCommit = revWalk.parseCommit(currentCommit.getParent(0));
            revWalk.parseBody(previousCommit); //

        }
        return convertCommitToInfo(previousCommit);


    }

    public CommitInfo restoreToPreviousCommit(Long documentId) throws IOException, GitAPIException {

        CommitInfo previousCommit = getPreviousCommit(documentId);

        return restoreToCommit(documentId, previousCommit.getId());


    }

    public CommitInfo restoreToCommit(Long documentId, String commitId, Authentication authentication) throws IOException, GitAPIException {
        File repoPath = new File(sourcePath, documentId.toString());
        Long userId = userService.getCurrentUserId(authentication);
        String username = userService.findById(userId).getUsername();
        RevCommit commited;
        try (Git git = Git.open(repoPath)) {
            Repository repository = git.getRepository(); // возвращаем объект для путешенствия по коммитам и деревьям

            ObjectId commitObjectId = repository.resolve(commitId); // из сокращенного айди, который есть в log получаем полный, с уоторым можно путешествовать по системе
            try (RevWalk revWalk = new RevWalk(repository)) {  // reyWalk позволяет путешестовать по коммитам
                RevCommit commit = revWalk.parseCommit(commitObjectId); // берем коммит за которым мы обратились
                revWalk.parseBody(commit);// получаем все данные об этом коммите
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
                    commited = git.commit()
                            .setMessage("Возврат")
                            .setAuthor(username, username + "@editor.local")
                            .call();
                }

            }
        }
        return convertCommitToInfo(commited);


    }

    public CommitInfo restoreToCommit(Long documentId, String commitId) throws IOException, GitAPIException {
        File repoPath = new File(sourcePath, documentId.toString());

        RevCommit commited;
        try (Git git = Git.open(repoPath)) {
            Repository repository = git.getRepository(); // возвращаем объект для путешенствия по коммитам и деревьям

            ObjectId commitObjectId = repository.resolve(commitId); // из сокращенного айди, который есть в log получаем полный, с уоторым можно путешествовать по системе
            try (RevWalk revWalk = new RevWalk(repository)) {  // reyWalk позволяет путешестовать по коммитам
                RevCommit commit = revWalk.parseCommit(commitObjectId); // берем коммит за которым мы обратились
                revWalk.parseBody(commit);
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
                    commited = git.commit()
                            .setMessage("Откат к версии " + commitId)
                            .call();
                }

            }
        }
        return convertCommitToInfo(commited);
    }

    public CommitInfo convertCommitToInfo(RevCommit commit) {
        String message;
        try {
            message = commit.getShortMessage();
        } catch (Exception e) {
            message = "(сообщение отсутствует)";
        }
        return CommitInfo.builder()
                .id(commit.getName())
                .message(message)
                .author(commit.getAuthorIdent().getName())
                .date(Instant.ofEpochSecond(commit.getCommitTime()))
                .build();
    }

    public CommitInfo commitToUserBranch(String texContent ,Long documentId ,String authorName, RevCommit initial )throws IOException,GitAPIException {
        RevCommit commit;
        Path repoPath = Paths.get(sourcePath, documentId.toString()).toAbsolutePath().normalize();
        String branchName = "user-" + authorName;

        try (Git git = Git.open(repoPath.toFile())) {
            if (git.getRepository().findRef(branchName) == null) {

                git.branchCreate().setName(branchName).setStartPoint(initial.getId().getName()).call();
            }
            git.checkout().setName(branchName).call();

            Path textile = repoPath.resolve("main.tex");
            Files.writeString(textile, texContent);

            git.add().addFilepattern("main.tex").call();

             commit = git.commit()
                    .setMessage("Внесены изменения")
                    .setAuthor(authorName, authorName + "@editor.local")
                     // любой email для Git
                    .call();

        }
        return convertCommitToInfo(commit);
        }
    public CommitInfo commitToUserBranch(String texContent, Long documentId, String authorName) throws IOException, GitAPIException {
        Path repoPath = Paths.get(sourcePath, documentId.toString()).toAbsolutePath().normalize();
        Long userId = userService.findByUsername(authorName).getId();

        // 1. Если репозиторий не инициализирован — создаём с первым коммитом
        if (!Files.exists(repoPath.resolve(".git"))) {
            initializeGitWithFirstCommit(documentId, texContent, authorName);
        }

        // 2. Получаем или создаём ветку
        String branchName = userDocumentRepository.findByUserIdAndDocumentId(userId, documentId)
                .map(UserDocument::getBranchName)
                .orElseGet(() -> {
                    String newBranch;
                    try {
                        if (hasMainBranch(documentId)) {
                            newBranch = createNewUserBranchFromMain(documentId, authorName);
                        } else {
                            newBranch = createInitialUserBranch(documentId, authorName);
                        }
                    } catch (Exception e) {
                        throw new RuntimeException("Не удалось создать ветку", e);
                    }

                    User user = userService.findById(userId);
                    Document document = documentService.findById(documentId);

                    UserDocument userDoc = new UserDocument();
                    userDoc.setId(new UserDocumentId(userId, documentId));
                    userDoc.setUser(user);
                    userDoc.setDocument(document);
                    userDoc.setBranchName(newBranch);
                    userDoc.setPermissionLevel("write");
                    userDoc.setAddedAt(Instant.now());

                    userDocumentRepository.save(userDoc);

                    return newBranch;
                });

        // 3. Работаем с Git-репозиторием
        try (Git git = Git.open(repoPath.toFile())) {
            // Если ветка в БД есть, но в Git нет — создаём заново
            if (git.getRepository().findRef(branchName) == null) {
                branchName = hasMainBranch(documentId)
                        ? createNewUserBranchFromMain(documentId, authorName)
                        : createInitialUserBranch(documentId, authorName);
                userDocumentRepository.updateBranchName(userId, documentId, branchName);
            }

            // Переключаемся на ветку
            git.checkout().setName(branchName).call();

            // Обновляем содержимое файла
            Path texFile = repoPath.resolve("main.tex");
            Files.writeString(texFile, texContent);

            git.add().addFilepattern("main.tex").call();

            RevCommit commit = git.commit()
                    .setMessage("Внесены изменения")
                    .setAuthor(authorName, authorName + "@editor.local")
                    .call();

            return convertCommitToInfo(commit);
        }
    }


    public boolean hasAnyCommit(Long documentId) throws IOException {
        Path repoPath = Paths.get(sourcePath, documentId.toString()).toAbsolutePath().normalize();
        try (Git git = Git.open(repoPath.toFile())) {
            return git.log().setMaxCount(1).call().iterator().hasNext();
        } catch (GitAPIException e) {
            throw new IOException("Ошибка при проверке наличия коммитов", e);
        }
    }
    public String createBranchFromFirstCommit(Long documentId, String username) throws IOException, GitAPIException {
        Path repoPath = Paths.get(sourcePath, documentId.toString()).toAbsolutePath().normalize();
        try (Git git = Git.open(repoPath.toFile())) {
            Iterable<RevCommit> commits = git.log().call();
            RevCommit firstCommit = null;
            for (RevCommit c : commits) {
                firstCommit = c; // итерация вернёт сначала новые, в конце — первый
            }
            if (firstCommit == null) {
                throw new IllegalStateException("Нет ни одного коммита в репозитории");
            }

            String branchName = "user-" + username;
            git.checkout()
                    .setStartPoint(firstCommit.getName())
                    .setName(branchName)
                    .setCreateBranch(true)
                    .call();

            return branchName;
        }
    }



    public String mergeUserBranchToMain(Long documentId, String authorName) throws IOException, GitAPIException {
        Path repoPath = Paths.get(sourcePath, documentId.toString()).toAbsolutePath().normalize();
        String userBranch = "user-" + authorName;
        String mainBranch = "main";

        try (Git git = Git.open(repoPath.toFile())) {
            git.checkout().setName(userBranch).call();

            MergeResult pullResult = git.merge()
                    .include(git.getRepository().findRef(mainBranch))
                    .setFastForward(MergeCommand.FastForwardMode.NO_FF)
                    .setCommit(true)
                    .setMessage("Подтягивание изменений из main в " + userBranch)
                    .call();

            if (pullResult.getMergeStatus().equals(MergeResult.MergeStatus.CONFLICTING)) {
                System.out.println("Конфликты при подтягивании main → user: " + pullResult.getConflicts());
            }

            git.checkout().setName(mainBranch).call();

            MergeResult finalMerge = git.merge()
                    .include(git.getRepository().findRef(userBranch))
                    .setFastForward(MergeCommand.FastForwardMode.NO_FF)
                    .setCommit(true)
                    .setMessage("Слияние ветки " + userBranch + " в main")
                    .call();

            if (finalMerge.getMergeStatus().isSuccessful()) {
                String newBranchName = createNewUserBranchFromMain(documentId, authorName);
                Long userId = userService.findByUsername(authorName).getId();
                userDocumentRepository.updateBranchName(userId, documentId, newBranchName);
                System.out.println("Создана новая ветка: " + newBranchName);
                return newBranchName;
            } else {
                throw new IllegalStateException("Merge неуспешен: " + finalMerge.getMergeStatus());
            }
        }
    }


    public boolean  mergeUserBranchToMaster(Long documentId,String authorName) throws IOException,GitAPIException
        {
            Path repoPath = Paths.get(sourcePath, documentId.toString()).toAbsolutePath().normalize();
            String branchName = "user-" + authorName;
            try( Git git = Git.open(repoPath.toFile()))
            {
                git.checkout().setName("master").call();

                MergeResult result = git.merge()
                        .include(git.getRepository().findRef(branchName))
                        .setCommit(true)
                        .setFastForward(MergeCommand.FastForwardMode.NO_FF)
                        .setMessage("Merge ветки " + branchName + " в master")
                        .call();
                return result.getMergeStatus().isSuccessful();


            }

        }

    public String createNewUserBranchFromMain(Long documentId, String username) throws IOException, GitAPIException {
        Path repoPath = Paths.get(sourcePath, documentId.toString()).toAbsolutePath().normalize();
        try (Git git = Git.open(repoPath.toFile()))
        {
            if( git.getRepository().findRef("main")==null)
            {
                throw new IllegalStateException("main не существует — нельзя создать ветку.");

            }

            String newBranchName =generateSufForUserBranch(documentId, username);

            git.checkout().setName("main").call();
            git.branchCreate().setName(newBranchName).setStartPoint("main").call();
            git.checkout().setName(newBranchName).call();

            return  newBranchName;
        }



    }
    public String generateSufForUserBranch(Long documentId, String username) throws IOException {
        Path repoPath = Paths.get(sourcePath, documentId.toString()).toAbsolutePath().normalize();
        try (Git git = Git.open(repoPath.toFile())) {
            Set<String> versionedBranches = git.branchList().call().stream()
                    .map(ref -> ref.getName().replace("refs/heads/", ""))
                    .filter(name -> name.matches("user-" + username + "-v\\d+"))
                    .collect(Collectors.toSet());

            // Если базовая ветка ещё не существует — создаём user-<имя>
            if (git.getRepository().findRef("user-" + username) == null) {
                return "user-" + username;
            }

            int suffix = 1;
            while (versionedBranches.contains("user-" + username + "-v" + suffix)) {
                suffix++;
            }
            return "user-" + username + "-v" + suffix;
        } catch (GitAPIException e) {
            throw new IOException("Ошибка при получении списка веток", e);
        }
    }







}




