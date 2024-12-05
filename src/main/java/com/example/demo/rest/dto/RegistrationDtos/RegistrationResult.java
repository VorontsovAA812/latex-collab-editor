package com.example.demo.rest.dto.RegistrationDtos;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationResult {
    private boolean success;
    private String message;

    // Конструкторы, геттеры и сеттеры
}
