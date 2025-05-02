package com.example.demo.rest.controllers;

import com.example.demo.exception.LatexCompilationException;
import com.example.demo.service.DocumentService;
import com.example.demo.service.impl.LatexService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.example.demo.rest.dto.DocumentDtos.LatexCompileRequest;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;



import java.io.IOException;

@Controller
@RequestMapping("/api/v1/documents")
public class LatexController {


    private final LatexService latexService;
    @Autowired

    public LatexController(LatexService latexService) {
        this.latexService = latexService;

    }

    @PostMapping("/compile")
    public ResponseEntity<Resource> compileLaTeX(@RequestBody LatexCompileRequest request) throws IOException, InterruptedException,LatexCompilationException  {



        return ResponseEntity.ok()
                .body(latexService.compileLaTeX(request));

}




    };
    //потом наверное стоит эти два эндпоинта объединить в один
    // но так пока проще теситровать и разрабатывать







