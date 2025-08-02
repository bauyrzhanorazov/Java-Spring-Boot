package com.example.javaspring.exception;

public abstract class TaskFlowException extends RuntimeException {

    protected TaskFlowException(String message) {
        super(message);
    }

    protected TaskFlowException(String message, Throwable cause) {
        super(message, cause);
    }
}