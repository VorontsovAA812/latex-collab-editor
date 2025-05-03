package com.example.demo.rest.dto.DocumentDtos;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class LatexResponseBuilder {

    public static ResponseEntity<Resource> buildPdfResponse(String filenamePdf, Resource pdf) {
        HttpHeaders headers = new HttpHeaders(); // создаем MULTYMAP для заголовков
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition
                .builder("inline")
                .filename(filenamePdf, StandardCharsets.UTF_8).build());
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdf);
    }
}
