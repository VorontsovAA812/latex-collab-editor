package com.example.demo.git;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class LatexCommitRequest {
    private Long documentId;
    private String texContent;
    private String authorName;
}

