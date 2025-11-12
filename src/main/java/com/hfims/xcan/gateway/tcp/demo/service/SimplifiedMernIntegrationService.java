package com.hfims.xcan.gateway.tcp.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Simplified MERN Integration Service
 * Focuses only on frontend-to-device operations
 * Real-time attendance events go directly from device to MERN backend
 */
@Service
public class SimplifiedMernIntegrationService {
    
    private final RestTemplate restTemplate;
    
    @Value("${mern.backend.url:http://localhost:3001}")
    private String mernBackendUrl;
    
    public SimplifiedMernIntegrationService() {
        this.restTemplate = new RestTemplate();
    }
    
    /**
     * Notify MERN backend of successful employee sync to device
     * Used only for employee registration/management operations
     */
    public ResponseEntity<String> notifyEmployeeSync(Map<String, Object> employeeData, boolean success, String message) {
        try {
            String endpoint = success ? "/api/employees/device-sync-success" : "/api/employees/device-sync-failure";
            String url = mernBackendUrl + endpoint;
            
            Map<String, Object> payload = new HashMap<>();
            payload.put("employeeData", employeeData);
            payload.put("timestamp", System.currentTimeMillis());
            payload.put("source", "JAVA_DEVICE_BRIDGE");
            
            if (success) {
                payload.put("deviceSyncResult", message);
            } else {
                payload.put("error", message);
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            
            return restTemplate.postForEntity(url, request, String.class);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Failed to notify MERN backend: " + e.getMessage() + "\"}");
        }
    }
    
    /**
     * Get employee data from MERN backend for device operations
     */
    public ResponseEntity<String> getEmployeeFromBackend(String employeeId) {
        try {
            String url = mernBackendUrl + "/api/employees/by-employee-id/" + employeeId;
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<?> request = new HttpEntity<>(headers);
            
            return restTemplate.exchange(url, HttpMethod.GET, request, String.class);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Failed to get employee data: " + e.getMessage() + "\"}");
        }
    }
    
    /**
     * Update device status in MERN backend
     */
    public ResponseEntity<String> updateDeviceStatus(Map<String, Object> deviceStatus) {
        try {
            String url = mernBackendUrl + "/api/device/status-update";
            
            Map<String, Object> payload = new HashMap<>();
            payload.put("deviceStatus", deviceStatus);
            payload.put("timestamp", System.currentTimeMillis());
            payload.put("source", "JAVA_DEVICE_BRIDGE");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            
            return restTemplate.postForEntity(url, request, String.class);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Failed to update device status: " + e.getMessage() + "\"}");
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
}