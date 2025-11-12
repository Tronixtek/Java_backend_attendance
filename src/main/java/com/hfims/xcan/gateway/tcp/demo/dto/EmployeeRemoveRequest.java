package com.hfims.xcan.gateway.tcp.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request DTO for removing employees from XO5 device
 */
public class EmployeeRemoveRequest {
    
    @JsonProperty("employeeId")
    private String employeeId;
    
    @JsonProperty("deviceKey")
    private String deviceKey;
    
    @JsonProperty("secret")
    private String secret;
    
    // Constructors
    public EmployeeRemoveRequest() {}
    
    public EmployeeRemoveRequest(String employeeId, String deviceKey, String secret) {
        this.employeeId = employeeId;
        this.deviceKey = deviceKey;
        this.secret = secret;
    }
    
    // Getters and Setters
    public String getEmployeeId() {
        return employeeId;
    }
    
    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }
    
    public String getDeviceKey() {
        return deviceKey;
    }
    
    public void setDeviceKey(String deviceKey) {
        this.deviceKey = deviceKey;
    }
    
    public String getSecret() {
        return secret;
    }
    
    public void setSecret(String secret) {
        this.secret = secret;
    }
    
    // Validation helper
    public boolean hasValidData() {
        return employeeId != null && !employeeId.trim().isEmpty() &&
               deviceKey != null && deviceKey.length() >= 16 &&
               secret != null && !secret.trim().isEmpty();
    }
    
    @Override
    public String toString() {
        return "EmployeeRemoveRequest{" +
                "employeeId='" + employeeId + '\'' +
                ", deviceKey='" + (deviceKey != null ? deviceKey.substring(0, 8) + "..." : null) + '\'' +
                '}';
    }
}