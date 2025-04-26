package com.example.demo.git;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommitInfo {
    private String id;
    private String message;
    private String author;
    private Instant date;
}

