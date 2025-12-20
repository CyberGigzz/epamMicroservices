package com.gym.crm.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.gym.crm.dto.error.ErrorResponseDto;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        fieldError -> fieldError.getField(),
                        fieldError -> fieldError.getDefaultMessage()
                ));
        
        LOGGER.warn("Validation failed: {}", errors);
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorResponseDto> handleEntityNotFound(EntityNotFoundException ex) {
        ErrorResponseDto error = new ErrorResponseDto(ex.getMessage());
        LOGGER.warn("Entity not found: {}", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }


    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorResponseDto> handleGenericException(Exception ex) {
        ErrorResponseDto error = new ErrorResponseDto("An unexpected internal server error occurred.");
        LOGGER.error("An unexpected error occurred: ", ex); 
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(AuthenticationFailedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<ErrorResponseDto> handleAuthenticationFailed(AuthenticationFailedException ex) {
        ErrorResponseDto error = new ErrorResponseDto(ex.getMessage());
        LOGGER.warn("Authentication failed: {}", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(DataConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ErrorResponseDto> handleDataConflict(DataConflictException ex) {
        ErrorResponseDto error = new ErrorResponseDto(ex.getMessage());
        LOGGER.warn("Data conflict: {}", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ErrorResponseDto> handleDatabaseConflict(DataIntegrityViolationException ex) {
        String message = "Database conflict: " + ex.getMostSpecificCause().getMessage();
        ErrorResponseDto error = new ErrorResponseDto(message);
        LOGGER.warn("Data integrity violation: {}", message);
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }
}