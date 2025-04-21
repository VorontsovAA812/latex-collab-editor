package com.example.demo.exception;


public class LatexCompilationException extends RuntimeException {
    private final String compilerOutput;

    public LatexCompilationException(String message, String compilerOutput) {
        super(message);
        this.compilerOutput = compilerOutput;
    }

    public String getCompilerOutput() {
        return compilerOutput;
    }
}
