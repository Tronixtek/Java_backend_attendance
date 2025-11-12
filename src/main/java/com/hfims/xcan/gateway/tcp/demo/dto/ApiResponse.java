package com.hfims.xcan.gateway.tcp.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Standardized API response wrapper for all endpoints
 */
public class ApiResponse<T> {
    
    @JsonProperty("success")
    private boolean success;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("data")
    private T data;
    
    @JsonProperty("code")
    private String code;
    
    @JsonProperty("timestamp")
    private long timestamp;
    
    // Constructors
    public ApiResponse() {
        this.timestamp = System.currentTimeMillis();
    }
    
    public ApiResponse(boolean success, String message) {
        this();
        this.success = success;
        this.message = message;
    }
    
    public ApiResponse(boolean success, String message, T data) {
        this(success, message);
        this.data = data;
    }
    
    public ApiResponse(boolean success, String message, String code, T data) {
        this(success, message, data);
        this.code = code;
    }
    
    // Static factory methods
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, "200", data);
    }
    
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message, "200", null);
    }
    
    public static <T> ApiResponse<T> error(String message, String code) {
        return new ApiResponse<>(false, message, code, null);
    }
    
    public static <T> ApiResponse<T> error(String message, String code, T data) {
        return new ApiResponse<>(false, message, code, data);
    }
    
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, "500", null);
    }
    
    public static <T> ApiResponse<T> error(String message, T data) {
        return new ApiResponse<>(false, message, "500", data);
    }
    
    public static <T> ApiResponse<T> validationError(String message) {
        return new ApiResponse<>(false, message, "400", null);
    }
    
    public static <T> ApiResponse<T> deviceError(String message) {
        return new ApiResponse<>(false, message, "1002", null);
    }
    
    public static <T> ApiResponse<T> partialError(String message, T data) {
        return new ApiResponse<>(false, message, "206", data);
    }
    
    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public T getData() {
        return data;
    }
    
    public void setData(T data) {
        this.data = data;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    @Override
    public String toString() {
        return "ApiResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", code='" + code + '\'' +
                ", timestamp=" + timestamp +
                ", hasData=" + (data != null) +
                '}';
    }
}