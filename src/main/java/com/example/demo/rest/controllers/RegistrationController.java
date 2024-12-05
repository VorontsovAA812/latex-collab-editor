package com.example.demo.rest.controllers;

import com.example.demo.domain.User;
import com.example.demo.rest.dto.RegistrationDtos.RegistrationResult;
import com.example.demo.rest.dto.UserDtos.NewUserRequest;
import com.example.demo.rest.dto.UserDtos.UserDto;
import com.example.demo.service.RegistrationService;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.sql.RowSet;

@Controller
@RequestMapping("/register")
public class RegistrationController  {

    @Autowired
    private RegistrationService registrationService;
    @Autowired
    private UserService userService;
    @GetMapping
    public String showRegisterPage(Model model) {
        model.addAttribute("text", "Добро пожаловать на страницу регистрации!"); // Добавляем данные в модель

        return "register"; // Имя файла шаблона: register.html
    }
    @PostMapping
    public String registerUser(@RequestParam String username,
                               @RequestParam String password,
                               Model model) {
        RegistrationResult result = registrationService.registerUser(username, password);

        if (!result.isSuccess()) {
            model.addAttribute("error", result.getMessage());
            return "register";
        }

        model.addAttribute("success", result.getMessage());
        return "login";
    }



}
