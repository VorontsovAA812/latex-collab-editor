package com.example.demo.git;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GitResponse {

    private String status;
    private String message;
    private String commitId;
}
