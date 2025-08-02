package com.example.javaspring.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class AccessDeniedException extends TaskFlowException {

    public AccessDeniedException(String message) {
        super(message);
    }

    public AccessDeniedException(String action, String resource) {
        super(String.format("Access denied to %s %s", action, resource));
    }
}