package com.edutool.exception;

import java.util.HashMap;
import java.util.Map;

public class ValidationException extends RuntimeException {
    
    private final Map<String, String> errors;
    
    public ValidationException(String message) {
        super(message);
        this.errors = new HashMap<>();
    }
    
    public ValidationException(String message, Map<String, String> errors) {
        super(message);
        this.errors = errors != null ? errors : new HashMap<>();
    }
    
    public ValidationException(Map<String, String> errors) {
        super("Validation failed");
        this.errors = errors != null ? errors : new HashMap<>();
    }
    
    public Map<String, String> getErrors() {
        return errors;
    }
    
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
    
    public void addError(String field, String message) {
        this.errors.put(field, message);
    }
}
