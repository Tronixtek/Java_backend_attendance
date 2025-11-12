package com.hfims.xcan.gateway.tcp.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Arrays;

/**
 * Request DTO for syncing employees to XO5 device
 * Compatible with both MERN backend requests and direct Java API calls
 */
public class EmployeeSyncRequest {
    
    // Core employee identification
    @JsonProperty("personId")
    private String personId; // Device ID for XO5 enrollment
    
    @JsonProperty("employeeId") 
    private String employeeId; // MERN employee ID
    
    @JsonProperty("name")
    private String name; // Full name for XO5 device
    
    @JsonProperty("fullName") 
    private String fullName; // Alternative full name field
    
    // Face recognition data
    @JsonProperty("faceImages")
    private List<String> faceImages; // Multiple face images for better recognition
    
    @JsonProperty("faceImage") 
    private String faceImage; // Single face image (base64)
    
    // Device credentials
    @JsonProperty("deviceKey")
    private String deviceKey;
    
    @JsonProperty("secret")
    private String secret;
    
    // Additional employee information
    @JsonProperty("department")
    private String department;
    
    @JsonProperty("position")
    private String position;
    
    @JsonProperty("designation")
    private String designation;
    
    // Constructors
    public EmployeeSyncRequest() {}
    
    public EmployeeSyncRequest(String personId, String name, String deviceKey, String secret) {
        this.personId = personId;
        this.name = name;
        this.deviceKey = deviceKey;
        this.secret = secret;
    }
    
    // Getters and Setters
    public String getPersonId() {
        return personId;
    }
    
    public void setPersonId(String personId) {
        this.personId = personId;
    }
    
    public String getEmployeeId() {
        return employeeId;
    }
    
    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }
    
    public String getName() {
        return name != null ? name : fullName;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getFullName() {
        return fullName != null ? fullName : name;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public List<String> getFaceImages() {
        if (faceImages != null && !faceImages.isEmpty()) {
            return faceImages;
        }
        
        // Convert single faceImage to list if available
        if (faceImage != null && !faceImage.trim().isEmpty()) {
            return Arrays.asList(faceImage);
        }
        
        return null;
    }
    
    public void setFaceImages(List<String> faceImages) {
        this.faceImages = faceImages;
    }
    
    public String getFaceImage() {
        return faceImage;
    }
    
    public void setFaceImage(String faceImage) {
        this.faceImage = faceImage;
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
    
    public String getDepartment() {
        return department;
    }
    
    public void setDepartment(String department) {
        this.department = department;
    }
    
    public String getPosition() {
        return position != null ? position : designation;
    }
    
    public void setPosition(String position) {
        this.position = position;
    }
    
    public String getDesignation() {
        return designation;
    }
    
    public void setDesignation(String designation) {
        this.designation = designation;
    }
    
    // Validation helper
    public boolean hasValidData() {
        return (personId != null && !personId.trim().isEmpty()) &&
               (getName() != null && !getName().trim().isEmpty()) &&
               (getFaceImages() != null && !getFaceImages().isEmpty()) &&
               (deviceKey != null && deviceKey.length() >= 16) &&
               (secret != null && !secret.trim().isEmpty());
    }
    
    @Override
    public String toString() {
        return "EmployeeSyncRequest{" +
                "personId='" + personId + '\'' +
                ", employeeId='" + employeeId + '\'' +
                ", name='" + getName() + '\'' +
                ", deviceKey='" + (deviceKey != null ? deviceKey.substring(0, 8) + "..." : null) + '\'' +
                ", department='" + department + '\'' +
                ", position='" + getPosition() + '\'' +
                ", faceImageCount=" + (getFaceImages() != null ? getFaceImages().size() : 0) +
                '}';
    }
}