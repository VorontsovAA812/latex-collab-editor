package com.example.demo.rest.controllers;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.example.demo.rest.dto.DocumentDtos.LatexCompileRequest;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/v1/documents")
public class LatexController {

    private final String LATEX_FILES_DIR = "./latex-files";
    private final String CONTAINER_NAME = "latex-compiler";

    @PostMapping("/compile")
    public ResponseEntity<Resource> compileLaTeX(@RequestBody LatexCompileRequest request) throws Exception {

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



        // Запуск процесса компиляции
        Process process = Runtime.getRuntime().exec(compileCommand);
        process.waitFor();

        HttpHeaders headers = new HttpHeaders(); // создаем MULTYMAP для заголовков

        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition
                .builder("inline")
                .filename(filenamePdf).build());


        return ResponseEntity.ok()
                .headers(headers)
                .body(new FileSystemResource(pdfFilePath.toFile()));

}




    };
    //потом наверное стоит эти два эндпоинта объединить в один
    // но так пока проще теситровать и разрабатывать







