package com.example.demo.rest.controllers;

import com.example.demo.exception.LatexCompilationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.example.demo.rest.dto.DocumentDtos.LatexCompileRequest;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/v1/documents")
public class LatexController {
    private static final Logger logger = LoggerFactory.getLogger(LatexController.class); // Добавляем логгер
    private final String LATEX_FILES_DIR = "./latex-files";
    private final String CONTAINER_NAME = "latex-compiler";

    @PostMapping("/compile")
    public ResponseEntity<Resource> compileLaTeX(@RequestBody LatexCompileRequest request) throws IOException, InterruptedException,LatexCompilationException  {

        String documentId = Long.toString(request.getId());
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




    };
    //потом наверное стоит эти два эндпоинта объединить в один
    // но так пока проще теситровать и разрабатывать







