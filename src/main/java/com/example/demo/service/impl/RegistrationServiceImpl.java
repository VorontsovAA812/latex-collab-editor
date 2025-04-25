package com.example.demo.service.impl;

import com.example.demo.domain.User;
import com.example.demo.repos.UserRepo;
import com.example.demo.rest.dto.RegistrationDtos.RegistrationResult;
import com.example.demo.rest.dto.UserDtos.NewUserRequest;
import com.example.demo.rest.dto.UserDtos.UserDto;
import com.example.demo.service.RegistrationService;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Instant;
import java.util.Optional;

@Service
public class RegistrationServiceImpl implements RegistrationService {
    @Autowired
    private UserRepo userRepo;


    @Override
    public User findByUsername(String username) {
        User user;
        Optional<User> optionalUser = userRepo.findByUsername(username);
        if (optionalUser.isPresent()) {
            user = optionalUser.get();
        } else {
            return null;
        }
        return  user;
    }

    @Transactional
    @Override
    public RegistrationResult registerUser(String username, String password)
    {
        User existingUser = userRepo.findByUsername(username).orElse(null);
        if (existingUser != null) {
        return new RegistrationResult(false, "Пользователь с таким именем уже существует!");
    }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(password); // Рекомендуется хранить пароль в зашифрованном виде
        newUser.setCreatedAt(Instant.now());  // устанавливаем время регистрации

        userRepo.save(newUser);

        return new RegistrationResult(true, "Пользователь успешно зарегистрирован!");
    }
    }

