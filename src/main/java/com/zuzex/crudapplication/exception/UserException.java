package com.zuzex.crudapplication.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;
@RequiredArgsConstructor
@Setter
@Getter
public class UserException extends RuntimeException {
    private final String message;
    private final HttpStatus status;
}
