package com._talent.lets_play.controllers;

import com._talent.lets_play.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
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


    @ExceptionHandler(InternalAuthenticationServiceException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED) // Changer aussi le status code
    public ResponseEntity<ErrorResponse> handleInternalAuthenticationException(
            InternalAuthenticationServiceException ex, // ← Bon type maintenant
            WebRequest request) {

        String path = ((ServletWebRequest) request).getRequest().getRequestURI();
        ErrorResponse errorResponse = new ErrorResponse.Builder()
                .withCode("AUTHENTICATION_ERROR") // Changer le code aussi
                .withMessage("Erreur d'authentification: " + ex.getMessage())
                .withStatus(HttpStatus.UNAUTHORIZED.value()) // 401 plus approprié
                .withTimestamp(LocalDateTime.now())
                .withPath(path)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

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
                .withFieldError("error registration:", ex.getMessage())
                .withPath(path)
                .build();


        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // ✅ CORRECTION: Bon type de paramètre pour Spring's IncorrectResultSizeDataAccessException
    @ExceptionHandler(org.springframework.dao.IncorrectResultSizeDataAccessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleIncorrectResultSizeException(
            org.springframework.dao.IncorrectResultSizeDataAccessException ex, WebRequest request) {

        String path = ((ServletWebRequest) request).getRequest().getRequestURI();

        ErrorResponse errorResponse = new ErrorResponse.Builder()
                .withCode("DATA_INTEGRITY_ERROR")
                .withMessage("Données incohérentes détectées - plusieurs résultats trouvés")
                .withStatus(HttpStatus.BAD_REQUEST.value())
                .withTimestamp(LocalDateTime.now())
                .withPath(path)
                .withFieldError("database", "Non-unique result found")
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // ✅ GESTION de votre exception personnalisée si vous préférez l'utiliser
    @ExceptionHandler(com._talent.lets_play.exception.IncorrectResultSizeDataAccessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleCustomIncorrectResultSizeException(
            com._talent.lets_play.exception.IncorrectResultSizeDataAccessException ex, WebRequest request) {

        String path = ((ServletWebRequest) request).getRequest().getRequestURI();

        ErrorResponse errorResponse = new ErrorResponse.Builder()
                .withCode("REGISTRATION_ERROR")
                .withMessage(ex.getMessage())
                .withStatus(HttpStatus.BAD_REQUEST.value())
                .withTimestamp(LocalDateTime.now())
                .withPath(path)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidExceptionException(MethodArgumentNotValidException ex, WebRequest request) {
        String path = ((ServletWebRequest) request).getRequest().getRequestURI();
        ErrorResponse.Builder errorResponse = new ErrorResponse.Builder()
                .withCode("INTERNAL_SERVER_ERROR")
                .withMessage("Une erreur interne s'est produite : ")
                .withStatus(HttpStatus.BAD_REQUEST.value())
                .withTimestamp(LocalDateTime.now())
                .withPath(path);
        ex.getBindingResult().getFieldErrors().forEach(fieldError -> errorResponse.withFieldError(fieldError.getField(), fieldError.getDefaultMessage()));

        return new ResponseEntity<>(errorResponse.build(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, WebRequest request) {
        String path = ((ServletWebRequest) request).getRequest().getRequestURI();
        ErrorResponse errorResponse = new ErrorResponse.Builder()
                .withCode("INTERNAL_SERVER_ERROR")
                .withMessage("Une erreur interne s'est produite : " + ex.getMessage())
                .withStatus(HttpStatus.BAD_REQUEST.value())
                .withTimestamp(LocalDateTime.now())
                .withPath(path)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}