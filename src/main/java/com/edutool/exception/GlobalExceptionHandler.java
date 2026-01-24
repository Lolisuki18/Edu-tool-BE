package com.edutool.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.edutool.dto.response.BaseResponse;
import com.edutool.dto.response.ErrorDetail;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        
        List<ErrorDetail> errors = new ArrayList<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            String errorCode = error.getCode(); // Get validation error code if available
            errors.add(new ErrorDetail(fieldName, errorMessage, errorCode));
        });
        
        BaseResponse<Object> response = BaseResponse.error(
                HttpStatus.BAD_REQUEST.value(), 
                "Invalid request data", 
                errors);
        return ResponseEntity.badRequest().body(response);
    }
    
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<BaseResponse<Object>> handleConstraintViolationException(
            ConstraintViolationException ex) {
        
        List<ErrorDetail> errors = new ArrayList<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            String fieldName = violation.getPropertyPath().toString();
            String errorMessage = violation.getMessage();
            String errorCode = violation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName();
            errors.add(new ErrorDetail(fieldName, errorMessage, errorCode));
        }
        
        BaseResponse<Object> response = BaseResponse.error(
                HttpStatus.BAD_REQUEST.value(),
                "Invalid request parameters",
                errors);
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<BaseResponse<Object>> handleValidationException(
            ValidationException ex) {
        
        BaseResponse<Object> response;
        if (ex.hasErrors()) {
            List<ErrorDetail> errors = new ArrayList<>();
            ex.getErrors().forEach((field, message) -> {
                errors.add(new ErrorDetail(field, message, null));
            });
            response = BaseResponse.error(
                    HttpStatus.BAD_REQUEST.value(),
                    ex.getMessage(), 
                    errors);
        } else {
            response = BaseResponse.error(
                    HttpStatus.BAD_REQUEST.value(),
                    ex.getMessage());
        }
        
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<BaseResponse<Object>> handleIllegalArgumentException(
            IllegalArgumentException ex) {
        
        BaseResponse<Object> response = BaseResponse.error(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<BaseResponse<Object>> handleAccessDeniedException(
            AccessDeniedException ex) {
        
        BaseResponse<Object> response = BaseResponse.error(
                HttpStatus.FORBIDDEN.value(),
                "Access denied. You don't have permission to perform this action");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<Object>> handleGenericException(Exception ex) {
        BaseResponse<Object> response = BaseResponse.error(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}

