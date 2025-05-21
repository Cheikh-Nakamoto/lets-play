package com._talent.lets_play.controllers;

import com._talent.lets_play.exception.BadRequestException;
import com._talent.lets_play.exception.ErrorResponse;
import com._talent.lets_play.exception.ResourceNotFoundException;
import com._talent.lets_play.exception.UnauthorizedAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        String path = ((ServletWebRequest) request).getRequest().getRequestURI();
        ErrorResponse errorResponse = new ErrorResponse.Builder()
                .withCode("RESOURCE_NOT_FOUND")
                .withMessage(ex.getMessage())
                .withStatus(HttpStatus.NOT_FOUND.value())
                .withTimestamp(LocalDateTime.now())
                .withPath(path)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<ErrorResponse> handleUnauthorizedAccessException(UnauthorizedAccessException ex, WebRequest request) {
        String path = ((ServletWebRequest) request).getRequest().getRequestURI();
        ErrorResponse errorResponse = new ErrorResponse.Builder()
                .withCode("UNAUTHORIZED_ACCESS")
                .withMessage(ex.getMessage())
                .withStatus(HttpStatus.FORBIDDEN.value())
                .withTimestamp(LocalDateTime.now())
                .withPath(path)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleBadRequestException(BadRequestException ex, WebRequest request) {
        String path = ((ServletWebRequest) request).getRequest().getRequestURI();
        ErrorResponse errorResponse = new ErrorResponse.Builder()
                .withCode("BAD_REQUEST")
                .withMessage(ex.getMessage())
                .withStatus(HttpStatus.BAD_REQUEST.value())
                .withTimestamp(LocalDateTime.now())
                .withPath(path)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
        String path = ((ServletWebRequest) request).getRequest().getRequestURI();

        ErrorResponse.Builder builder = new ErrorResponse.Builder()
                .withCode("VALIDATION_ERROR")
                .withMessage("Error de validation des données")
                .withStatus(HttpStatus.BAD_REQUEST.value())
                .withTimestamp(LocalDateTime.now())
                .withPath(path);

        ex.getBindingResult().getFieldErrors().forEach(error ->
                builder.withFieldError(error.getField(), error.getDefaultMessage())
        );

        return new ResponseEntity<>(builder.build(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        String path = ((ServletWebRequest) request).getRequest().getRequestURI();
        ErrorResponse errorResponse = new ErrorResponse.Builder()
                .withCode("ACCESS_DENIED")
                .withMessage("Vows n'avez pas les permissions nécessaires pour accéder à cette ressource")
                .withStatus(HttpStatus.FORBIDDEN.value())
                .withTimestamp(LocalDateTime.now())
                .withPath(path)
                .withFieldError("Authorisation:", ex.getMessage())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, WebRequest request) {
        String path = ((ServletWebRequest) request).getRequest().getRequestURI();
        ErrorResponse errorResponse = new ErrorResponse.Builder()
                .withCode("INTERNAL_SERVER_ERROR")
                .withMessage("Une erreur interne s'est produite lors de l'authentification par spring: " + ex.getMessage())
                .withStatus(HttpStatus.BAD_REQUEST.value())
                .withTimestamp(LocalDateTime.now())
                .withPath(path)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}