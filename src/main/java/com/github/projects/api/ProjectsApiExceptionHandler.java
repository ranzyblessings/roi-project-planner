package com.github.projects.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

/**
 * Centralized exception handler for REST API endpoints, providing uniform error responses.
 */
@RestControllerAdvice
public class ProjectsApiExceptionHandler {

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ApiResponse<String>>> handleValidationExceptions(WebExchangeBindException ex) {
        var errorMessages = collectErrorMessages(ex);

        final var httpStatus = HttpStatus.BAD_REQUEST;
        var response = ApiResponse.<String>error(httpStatus.value(), errorMessages);
        return Mono.just(new ResponseEntity<>(response, httpStatus));
    }

    private static String collectErrorMessages(WebExchangeBindException ex) {
        return ex.getBindingResult().getFieldErrors().stream()
                .map(error -> String.format("%s: %s", error.getField(), error.getDefaultMessage()))
                .collect(Collectors.joining(", "));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ApiResponse<String>>> handleGenericException(Exception ex) {
        final var httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        var response = ApiResponse.<String>error(httpStatus.value(), ex.getMessage());
        return Mono.just(new ResponseEntity<>(response, httpStatus));
    }
}