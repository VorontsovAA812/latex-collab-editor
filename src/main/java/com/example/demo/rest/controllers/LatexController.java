package com.example.demo.rest.controllers;


import com.example.demo.rest.dto.DocumentDtos.LatexCompileRequest;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/v1/documents")
public class LatexController {

    private final String LATEX_FILES_DIR = "./latex-files";
    private final String CONTAINER_NAME = "latex-compiler";

    @PostMapping("/compile")
    public ResponseEntity<String> compileLaTeX(@RequestBody LatexCompileRequest request) throws Exception {

        String documentId  = Long.toString(request.getId()); // берем id из запроса и переводим его в String
        String workDir = LATEX_FILES_DIR + "/" + documentId ;   // создаем строку ./latex-files/documentId

        Files.createDirectories(Paths.get(workDir)); // создаем все папки(которые ещё не сущ) в пути /latex-files/documentId

        String latexSource = request.getContent(); // берем latex код в виде String
        String filename = request.getTitle();

        filename = filename + ".tex";
        Path texFilePath = Paths.get(workDir, filename); // создаем путь  ./latex-files/documentId/title.tex

        Files.writeString(texFilePath, latexSource); //создает файл tex и записыванет в него строку latexSourse


        // Команда для компиляции через Docker

        String containerPath = "/data/" + documentId;
        String texFile = "/data/" + documentId + "/"+ filename;
        String compileCommand = String.format(
                "docker exec %s pdflatex -interaction=nonstopmode -output-directory=%s %s",
                CONTAINER_NAME, containerPath, texFile
        );

        // Запуск процесса компиляции
        Process process = Runtime.getRuntime().exec(compileCommand);
        process.waitFor();

        return ResponseEntity.ok("Компиляция завершена");


    }


    };

