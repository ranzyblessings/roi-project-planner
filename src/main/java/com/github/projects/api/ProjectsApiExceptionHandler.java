package com.github.projects.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

/**
 * Centralized exception handler for REST API endpoints, providing uniform error responses.
 */
@RestControllerAdvice
public class ProjectsApiExceptionHandler {

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ApiResponse<String>>> handleValidationExceptions(WebExchangeBindException e) {
        String errorMessages = e.getBindingResult().getFieldErrors().stream()
                .map(error -> String.format("%s: %s", error.getField(), error.getDefaultMessage()))
                .collect(Collectors.joining(", "));

        var response = ApiResponse.<String>error(HttpStatus.BAD_REQUEST.value(), errorMessages);
        return Mono.just(ResponseEntity.badRequest().body(response));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public Mono<ResponseEntity<ApiResponse<String>>> handleResponseStatusException(ResponseStatusException e) {
        var response = ApiResponse.<String>error(e.getStatusCode().value(), e.getReason());
        return Mono.just(ResponseEntity.status(e.getStatusCode()).body(response));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ApiResponse<String>>> handleGenericException(Exception e) {
        var response = ApiResponse.<String>error(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response));
    }
}