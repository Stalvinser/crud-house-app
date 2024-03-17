package com.zuzex.crudapplication.security.auth;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;
@RequiredArgsConstructor
@Setter
@Getter
public class AuthenticationException extends RuntimeException {
    private final String message;
    private final HttpStatus status;
}
