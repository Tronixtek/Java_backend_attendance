package com.hfims.xcan.gateway.tcp.demo.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * MERN Backend Integration Service
 * Handles two-way communication between Java (XO5) and Node.js (Database)
 * Ensures transactional integrity for data operations
 */
@Service
public class MernIntegrationService {
    
    private final RestTemplate restTemplate;
    
    @Value("${mern.backend.url:http://localhost:3001}")
    private String mernBackendUrl;
    
    public MernIntegrationService() {
        this.restTemplate = new RestTemplate();
    }
    
    /**
     * Notify MERN backend of successful employee sync to device
     * This triggers the database save operation
     */
    public ResponseEntity<String> confirmEmployeeSyncSuccess(Map<String, Object> employeeData, String syncResult) {
        try {
            String url = mernBackendUrl + "/api/employees/device-sync-success";
            
            Map<String, Object> payload = new HashMap<>();
            payload.put("employeeData", employeeData);
            payload.put("deviceSyncResult", syncResult);
            payload.put("timestamp", System.currentTimeMillis());
            payload.put("source", "XO5_DEVICE");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            
            return restTemplate.postForEntity(url, request, String.class);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(JSON.toJSONString(Map.of("error", "Failed to notify MERN backend: " + e.getMessage())));
        }
    }
    
    /**
     * Notify MERN backend of failed employee sync to device
     * This prevents database save and triggers error handling
     */
    public ResponseEntity<String> confirmEmployeeSyncFailure(Map<String, Object> employeeData, String errorMessage) {
        try {
            String url = mernBackendUrl + "/api/employees/device-sync-failure";
            
            Map<String, Object> payload = new HashMap<>();
            payload.put("employeeData", employeeData);
            payload.put("error", errorMessage);
            payload.put("timestamp", System.currentTimeMillis());
            payload.put("source", "XO5_DEVICE");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            
            return restTemplate.postForEntity(url, request, String.class);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(JSON.toJSONString(Map.of("error", "Failed to notify MERN backend: " + e.getMessage())));
        }
    }
    
    /**
     * Send attendance data to MERN backend for database storage
     */
    public ResponseEntity<String> sendAttendanceRecord(Map<String, Object> attendanceData) {
        try {
            String url = mernBackendUrl + "/api/attendance/record";
            
            Map<String, Object> payload = new HashMap<>();
            payload.put("attendanceData", attendanceData);
            payload.put("timestamp", System.currentTimeMillis());
            payload.put("source", "XO5_DEVICE");
            payload.put("verified", true);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            
            return restTemplate.postForEntity(url, request, String.class);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(JSON.toJSONString(Map.of("error", "Failed to send attendance data: " + e.getMessage())));
        }
    }
    
    /**
     * Request employee data from MERN backend for device synchronization
     */
    public ResponseEntity<String> getEmployeeForDeviceSync(String employeeId) {
        try {
            String url = mernBackendUrl + "/api/employees/" + employeeId + "/device-data";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<?> request = new HttpEntity<>(headers);
            
            return restTemplate.exchange(url, HttpMethod.GET, request, String.class);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(JSON.toJSONString(Map.of("error", "Failed to get employee data: " + e.getMessage())));
        }
    }
    
    /**
     * Notify MERN backend of device status changes
     */
    public ResponseEntity<String> updateDeviceStatus(Map<String, Object> deviceStatus) {
        try {
            String url = mernBackendUrl + "/api/devices/status";
            
            Map<String, Object> payload = new HashMap<>();
            payload.put("deviceStatus", deviceStatus);
            payload.put("timestamp", System.currentTimeMillis());
            payload.put("deviceIP", "192.168.0.169");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            
            return restTemplate.postForEntity(url, request, String.class);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(JSON.toJSONString(Map.of("error", "Failed to update device status: " + e.getMessage())));
        }
    }
    
    /**
     * Health check with MERN backend
     */
    public boolean isMernBackendHealthy() {
        try {
            String url = mernBackendUrl + "/api/health";
            
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return response.getStatusCode() == HttpStatus.OK;
            
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Parse JSON response safely
     */
    public JSONObject parseResponse(String response) {
        try {
            return JSON.parseObject(response);
        } catch (Exception e) {
            JSONObject errorResponse = new JSONObject();
            errorResponse.put("error", "Failed to parse response");
            errorResponse.put("rawResponse", response);
            return errorResponse;
        }
    }
}