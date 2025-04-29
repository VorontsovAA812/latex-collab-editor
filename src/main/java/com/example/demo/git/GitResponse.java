package com.example.demo.git;


import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GitResponse {

    private String message;
    private Long documentId;
}
