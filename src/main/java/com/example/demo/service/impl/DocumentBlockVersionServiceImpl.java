package com.example.demo.service.impl;


import com.example.demo.repos.DocumentBlockRepo;
import com.example.demo.service.DocumentBlockVersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DocumentBlockVersionServiceImpl implements DocumentBlockVersionService {
    DocumentBlockRepo  documentBlockRepo;


    @Autowired
    public DocumentBlockVersionServiceImpl(DocumentBlockRepo documentBlockRepo) {
        this.documentBlockRepo = documentBlockRepo;
    }






}
