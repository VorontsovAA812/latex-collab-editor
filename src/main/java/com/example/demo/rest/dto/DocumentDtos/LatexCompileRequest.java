package com.example.demo.rest.dto.DocumentDtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;




@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LatexCompileRequest {
    private Long id;
    private String content;
    private String title;

}
