package org.example.config;

import org.example.exception.InsufficientDataException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "请求参数错误");
        response.put("errors", errors);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InsufficientDataException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, Object>> handleInsufficientDataException(InsufficientDataException ex) {
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("requiredDays", ex.getRequiredDays());
        errorDetails.put("availableDays", ex.getAvailableDays());

        Map<String, Object> error = new HashMap<>();
        error.put("code", "INSUFFICIENT_DATA");
        error.put("message", ex.getMessage());
        error.put("details", errorDetails);

        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", error);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}