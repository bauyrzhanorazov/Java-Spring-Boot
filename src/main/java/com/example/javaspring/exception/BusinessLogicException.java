package com.example.javaspring.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class BusinessLogicException extends TaskFlowException {

    public BusinessLogicException(String message) {
        super(message);
    }

    public BusinessLogicException(String operation, String reason) {
        super(String.format("Cannot %s: %s", operation, reason));
    }
}