package com.example.demo.rest.dto.DocumentDtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;



@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentResponse{

        private Long id;
        private String title;
        private String content;
        private String ownerUsername; // Только нужные данные о владельце
        private Instant createdAt;
        private Instant updatedAt; // Отображаем



}

