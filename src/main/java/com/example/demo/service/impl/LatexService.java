package com.example.demo.service.impl;


import com.example.demo.exception.LatexCompilationException;
import com.example.demo.rest.controllers.LatexController;
import com.example.demo.rest.dto.DocumentDtos.LatexCompileRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class LatexService {
    private static final Logger logger = LoggerFactory.getLogger(LatexController.class); // Добавляем логгер
    private final String LATEX_FILES_DIR = "./latex-files";
    private final String CONTAINER_NAME = "latex-compiler";


    public void copyForCompilation(Long documentId) throws IOException {
        String sourcePath = "./latex-versions";  // здесь git, main.tex

        String sourcePath2 = "./latex-files";  // здесь git, main.tex

        Path repoPath = Paths.get(sourcePath, documentId.toString()).toAbsolutePath().normalize();
        Path repoPath2 = Paths.get(sourcePath2, documentId.toString()).toAbsolutePath().normalize();

        Path textile= repoPath.resolve("main.tex");

        if (!Files.exists(textile)) {
            throw new FileNotFoundException("main.tex не найден по пути: " + textile);
        }

        Files.createDirectories(repoPath2); // вдруг нет папки
        Path textile2 = repoPath2.resolve("main.tex");

        Files.copy(textile, textile2, StandardCopyOption.REPLACE_EXISTING);


    }

    public ResponseEntity<Resource> compileLaTeX(@RequestBody LatexCompileRequest request) throws IOException, InterruptedException, LatexCompilationException {

        String documentId = Long.toString(request.getId());

        copyForCompilation(request.getId());




        // Используем абсолютный путь вместо относительного
        Path workDirPath = Paths.get(LATEX_FILES_DIR, documentId).toAbsolutePath().normalize();
        String workDir = workDirPath.toString();

        Files.createDirectories(workDirPath);

        String latexSource = request.getContent(); // берем latex код в виде String
        String filename = request.getTitle();


        String filenameTex = filename + ".tex";
        String filenamePdf = filename + ".pdf";

        Path texFilePath = Paths.get(workDir, filenameTex); // создаем путь  ./latex-files/documentId/title.tex
        Path pdfFilePath = Paths.get(workDir, filenamePdf); // создаем путь  ./latex-files/documentId/title.tex

        Files.writeString(texFilePath, latexSource); //создает файл tex и записыванет в него строку latexSourse
        // подумать над путями!!!

        // Команда для компиляции через Docker


        String containerPath = "/data/" + documentId;
        String texFile = "/data/" + documentId + "/"+ filenameTex;
        String compileCommand = String.format(
                "docker exec %s pdflatex -interaction=nonstopmode -output-directory=%s %s",
                CONTAINER_NAME, containerPath, texFile
        );

        // РЕЖИМ НОНСТОП НАДО ЛИ ИЛИ НЕТ?

        Process process = Runtime.getRuntime().exec(compileCommand);
        StringBuilder output = new StringBuilder();

        try (
                BufferedReader stdoutReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                BufferedReader stderrReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))
        ) {
            // Чтение стандартного вывода
            String line;
            while ((line = stdoutReader.readLine()) != null) {
                output.append(line).append("\n");
            }

            // Чтение вывода ошибок
            while ((line = stderrReader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        int exitCode = process.waitFor();

        if (exitCode != 0 || !Files.exists(pdfFilePath)) {
            String errorMessage = "Ошибка компиляции. Вывод:\n" + output.toString();
            logger.error(errorMessage); // Запись в лог
            System.err.println(errorMessage); // Вывод в консоль
            throw new LatexCompilationException(
                    "Ошибка компиляции LaTeX документа",
                    output.toString()
            );
        }

        HttpHeaders headers = new HttpHeaders(); // создаем MULTYMAP для заголовков

        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition
                .builder("inline")
                .filename(filenamePdf, StandardCharsets.UTF_8).build());


        return ResponseEntity.ok()
                .headers(headers)
                .body(new FileSystemResource(pdfFilePath.toFile()));

    }

}
