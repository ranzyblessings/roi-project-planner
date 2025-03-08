package com.github.projects.api;

import com.github.projects.exception.ProjectNotFoundException;
import com.github.projects.exception.TooManyProjectsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

/**
 * Centralized exception handler for REST API endpoints, providing uniform error responses.
 */
@RestControllerAdvice
public class ProjectsApiExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(ProjectsApiExceptionHandler.class);

    /**
     * Handles request validation errors and returns structured error messages.
     */
    @ExceptionHandler(WebExchangeBindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<ResponseEntity<ApiResponse<String>>> handleValidationExceptions(WebExchangeBindException e) {
        String errorMessages = e.getBindingResult().getFieldErrors().stream()
                .map(error -> String.format("%s: %s", error.getField(), error.getDefaultMessage()))
                .collect(Collectors.joining(", "));

        var response = ApiResponse.<String>error(HttpStatus.BAD_REQUEST.value(), errorMessages);
        return Mono.just(ResponseEntity.badRequest().body(response));
    }

    /**
     * Handles exceptions for exceeding the allowed project creation limit.
     */
    @ExceptionHandler(TooManyProjectsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<ResponseEntity<ApiResponse<String>>> handleTooManyProjectsException(TooManyProjectsException e) {
        var response = ApiResponse.<String>error(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        return Mono.just(ResponseEntity.badRequest().body(response));
    }

    /**
     * Handles exceptions when a project with the given ID is not found.
     */
    @ExceptionHandler(ProjectNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Mono<ResponseEntity<ApiResponse<String>>> handleProjectNotFoundException(ProjectNotFoundException e) {
        var response = ApiResponse.<String>error(HttpStatus.NOT_FOUND.value(), e.getMessage());
        return Mono.just(ResponseEntity.badRequest().body(response));
    }

    /**
     * Handles unexpected system errors by logging and returning a structured response.
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Mono<ResponseEntity<ApiResponse<String>>> handleGenericException(Exception e) {
        logger.error("An unexpected error occurred", e);
        var response = ApiResponse.<String>error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An unexpected error occurred.");
        return Mono.just(ResponseEntity.internalServerError().body(response));
    }
}