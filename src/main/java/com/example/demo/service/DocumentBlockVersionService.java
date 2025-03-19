package com.example.demo.service;

import com.example.demo.rest.dto.DocumentBlockVersionDtos.NewVersionRequest;
import org.springframework.security.core.Authentication;


public interface DocumentBlockVersionService {
     Long createNewVersion(NewVersionRequest request, Authentication authentication , Long blockId);



}
