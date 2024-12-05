package com.example.demo.service;

import com.example.demo.domain.User;
import com.example.demo.rest.dto.RegistrationDtos.RegistrationResult;
import com.example.demo.rest.dto.UserDtos.UserDto;

public interface RegistrationService {
    User findByUsername(String username);
   RegistrationResult registerUser(String username, String password);
}
