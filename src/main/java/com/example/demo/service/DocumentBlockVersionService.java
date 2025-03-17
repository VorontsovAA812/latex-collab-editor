package com.example.demo.service;

import com.example.demo.rest.dto.DocumentBlockVersionDtos.NewVersionRequest;
import org.springframework.stereotype.Service;

@Service
public interface DocumentBlockVersionService {
     Long createNewVersion(NewVersionRequest request, Long userId, Long blockId);



}
