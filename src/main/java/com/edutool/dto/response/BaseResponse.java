package com.edutool.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class BaseResponse<T> {
    
    private boolean isSuccess;
    
    private int code;
    
    private String message;
    
    private T data;
    
    private List<ErrorDetail> errors;
    
    private LocalDateTime timestamp;
    
    public BaseResponse(boolean isSuccess, int code, String message, T data, List<ErrorDetail> errors, LocalDateTime timestamp) {
        this.isSuccess = isSuccess;
        this.code = code;
        this.message = message;
        this.data = data;
        this.errors = errors != null ? errors : new ArrayList<>();
        this.timestamp = timestamp;
    }
    
    public BaseResponse(boolean isSuccess, int code, String message, T data, List<ErrorDetail> errors) {
        this.isSuccess = isSuccess;
        this.code = code;
        this.message = message;
        this.data = data;
        this.errors = errors != null ? errors : new ArrayList<>();
        this.timestamp = LocalDateTime.now();
    }
    
    // Success methods with default code 200
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(true, 200, "Success", data, null, LocalDateTime.now());
    }
    
    public static <T> BaseResponse<T> success(String message, T data) {
        return new BaseResponse<>(true, 200, message, data, null, LocalDateTime.now());
    }
    
    // Success methods with custom code
    public static <T> BaseResponse<T> success(int code, String message, T data) {
        return new BaseResponse<>(true, code, message, data, null, LocalDateTime.now());
    }
    
    // Error methods with default code 400
    public static <T> BaseResponse<T> error(String message) {
        return new BaseResponse<>(false, 400, message, null, null, LocalDateTime.now());
    }
    
    public static <T> BaseResponse<T> error(String message, T data) {
        return new BaseResponse<>(false, 400, message, data, null, LocalDateTime.now());
    }
    
    public static <T> BaseResponse<T> error(String message, List<ErrorDetail> errors) {
        return new BaseResponse<>(false, 400, message, null, errors, LocalDateTime.now());
    }
    
    // Error methods with custom code
    public static <T> BaseResponse<T> error(int code, String message) {
        return new BaseResponse<>(false, code, message, null, null, LocalDateTime.now());
    }
    
    public static <T> BaseResponse<T> error(int code, String message, T data) {
        return new BaseResponse<>(false, code, message, data, null, LocalDateTime.now());
    }
    
    public static <T> BaseResponse<T> error(int code, String message, List<ErrorDetail> errors) {
        return new BaseResponse<>(false, code, message, null, errors, LocalDateTime.now());
    }
    
    // Helper method to add error
    public void addError(String field, String message) {
        if (this.errors == null) {
            this.errors = new ArrayList<>();
        }
        this.errors.add(new ErrorDetail(field, message, null));
    }
    
    // Helper method to add error with code
    public void addError(String field, String message, String code) {
        if (this.errors == null) {
            this.errors = new ArrayList<>();
        }
        this.errors.add(new ErrorDetail(field, message, code));
    }
}
