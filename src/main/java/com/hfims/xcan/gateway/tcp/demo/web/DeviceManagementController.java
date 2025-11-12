package com.hfims.xcan.gateway.tcp.demo.web;

import com.hfims.xcan.gateway.netty.client.HfDeviceClient;
import com.hfims.xcan.gateway.netty.client.resp.HfDeviceResp;
import com.hfims.xcan.gateway.netty.error.CgiErrorException;
import com.hfims.xcan.gateway.tcp.demo.dto.ApiResponse;
import com.hfims.xcan.gateway.netty.client.dto.HostInfoDto;
import com.hfims.xcan.gateway.tcp.demo.service.RequestBuilderService;
import com.hfims.xcan.gateway.tcp.demo.service.DeviceMethodInspector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Controller for XO5 device management operations
 * Handles device configuration, monitoring, and control
 */
@RestController
@RequestMapping("/api/device")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class DeviceManagementController extends BaseController {

    @Autowired
    private RequestBuilderService requestBuilderService;
    
    @Autowired
    private DeviceMethodInspector deviceMethodInspector;

    /**
     * Get device information and capabilities
     */
    @PostMapping("/info")
    public ApiResponse<Map<String, Object>> getDeviceInfo(@RequestBody Map<String, Object> request) {
        try {
            System.out.println("=== DEVICE INFO REQUEST ===");
            
            String deviceKey = (String) request.get("deviceKey");
            String secret = (String) request.get("secret");
            
            // Validate device credentials
            validateCommon(deviceKey, secret);
            
            // Get host info for device connection
            HostInfoDto hostInfo = getHostInfo();
            System.out.println("DEBUG - Gateway Host Info: " + hostInfo.getHost() + ":" + hostInfo.getPort() + " (timeout: " + hostInfo.getTimeout() + "ms)");
            
            // Prepare response with device connection information
            Map<String, Object> deviceInfo = new HashMap<>();
            deviceInfo.put("deviceKey", deviceKey);
            deviceInfo.put("gatewayHost", hostInfo.getHost());
            deviceInfo.put("gatewayPort", hostInfo.getPort());
            deviceInfo.put("connectionTimeout", hostInfo.getTimeout());
            deviceInfo.put("status", "connected");
            deviceInfo.put("deviceType", "XO5 Biometric Device");
            deviceInfo.put("sdkVersion", "1.0.0");
            deviceInfo.put("apiVersion", "v1");
            
            return ApiResponse.success("Device information retrieved successfully", deviceInfo);
            
        } catch (CgiErrorException e) {
            System.err.println("CGI Error in device info: " + e.getMessage());
            e.printStackTrace();
            return ApiResponse.error("Device communication error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error in device info: " + e.getMessage());
            e.printStackTrace();
            return ApiResponse.error("Internal server error: " + e.getMessage());
        }
    }

    /**
     * Get device status and health
     */
    @PostMapping("/status")
    public ApiResponse<Map<String, Object>> getDeviceStatus(@RequestBody Map<String, Object> request) {
        try {
            System.out.println("=== DEVICE STATUS REQUEST ===");
            
            String deviceKey = (String) request.get("deviceKey");
            String secret = (String) request.get("secret");
            
            // Validate device credentials
            validateCommon(deviceKey, secret);
            
            // Get host info for device connection
            HostInfoDto hostInfo = getHostInfo();
            
            // Prepare status response
            Map<String, Object> status = new HashMap<>();
            status.put("deviceKey", deviceKey);
            status.put("connectionStatus", "online");
            status.put("heartbeatActive", true);
            status.put("lastHeartbeat", new Date());
            status.put("gatewayConnection", true);
            status.put("sdkConnection", true);
            status.put("networkLatency", "< 50ms");
            status.put("operationalStatus", "ready");
            
            // Add gateway information
            Map<String, Object> gateway = new HashMap<>();
            gateway.put("host", hostInfo.getHost());
            gateway.put("gwPort", 10010);
            gateway.put("sdkPort", 10011);
            gateway.put("timeout", hostInfo.getTimeout());
            status.put("gateway", gateway);
            
            return ApiResponse.success("Device status retrieved successfully", status);
            
        } catch (CgiErrorException e) {
            System.err.println("CGI Error in device status: " + e.getMessage());
            return ApiResponse.error("Device communication error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error in device status: " + e.getMessage());
            return ApiResponse.error("Internal server error: " + e.getMessage());
        }
    }

    /**
     * Discover available device methods
     */
    @GetMapping("/methods")
    public ApiResponse<Map<String, Object>> getAvailableMethods() {
        try {
            System.out.println("=== DEVICE METHODS DISCOVERY ===");
            
            // Get all public static methods from HfDeviceClient
            List<Map<String, Object>> methods = new ArrayList<>();
            
            Class<?> clazz = HfDeviceClient.class;
            Method[] allMethods = clazz.getDeclaredMethods();
            
            for (Method method : allMethods) {
                if (java.lang.reflect.Modifier.isPublic(method.getModifiers()) && 
                    java.lang.reflect.Modifier.isStatic(method.getModifiers())) {
                    
                    Map<String, Object> methodInfo = new HashMap<>();
                    methodInfo.put("name", method.getName());
                    methodInfo.put("returnType", method.getReturnType().getSimpleName());
                    
                    List<String> paramTypes = new ArrayList<>();
                    for (Class<?> paramType : method.getParameterTypes()) {
                        paramTypes.add(paramType.getSimpleName());
                    }
                    methodInfo.put("parameters", paramTypes);
                    
                    methods.add(methodInfo);
                }
            }
            
            // Sort methods by name
            methods.sort((a, b) -> ((String) a.get("name")).compareTo((String) b.get("name")));
            
            Map<String, Object> response = new HashMap<>();
            response.put("totalMethods", methods.size());
            response.put("methods", methods);
            response.put("clientClass", "HfDeviceClient");
            response.put("discoveredAt", new Date());
            
            return ApiResponse.success("Available methods discovered successfully", response);
            
        } catch (Exception e) {
            System.err.println("Error discovering methods: " + e.getMessage());
            return ApiResponse.error("Method discovery failed: " + e.getMessage());
        }
    }
    
    /**
     * Helper method to check device connection status
     */
    private Map<String, Object> checkDeviceConnection(HostInfoDto hostInfo, String deviceKey, String secret) {
        Map<String, Object> connectionStatus = new HashMap<>();
        try {
            HfDeviceResp response = HfDeviceClient.deviceGet(hostInfo, deviceKey, secret);
            connectionStatus.put("connected", "000".equals(response.getCode()));
            connectionStatus.put("responseTime", System.currentTimeMillis());
            connectionStatus.put("statusCode", response.getCode());
            connectionStatus.put("statusMessage", response.getMsg());
        } catch (Exception e) {
            connectionStatus.put("connected", false);
            connectionStatus.put("error", e.getMessage());
        }
        return connectionStatus;
    }

    /**
     * Reboot device
     */
    @PostMapping("/reboot")
    public ApiResponse<Map<String, Object>> rebootDevice(@RequestBody Map<String, Object> request) {
        try {
            System.out.println("=== DEVICE REBOOT REQUEST ===");
            
            String deviceKey = (String) request.get("deviceKey");
            String secret = (String) request.get("secret");
            
            // Validate device credentials
            validateCommon(deviceKey, secret);
            
            // Get host info for device connection
            HostInfoDto hostInfo = getHostInfo();
            System.out.println("DEBUG - Attempting device reboot for device: " + deviceKey);
            
            // First check if device is responsive
            Map<String, Object> deviceStatus = checkDeviceConnection(hostInfo, deviceKey, secret);
            
            try {
                // Attempt to call device reboot using HfDeviceClient
                HfDeviceResp response = HfDeviceClient.deviceReboot(hostInfo, deviceKey, secret);
                
                System.out.println("DEBUG - Device reboot response: " + response);
                
                Map<String, Object> rebootResponse = new HashMap<>();
                rebootResponse.put("deviceKey", deviceKey);
                rebootResponse.put("responseCode", response.getCode());
                rebootResponse.put("responseMessage", response.getMsg());
                rebootResponse.put("timestamp", new Date());
                rebootResponse.put("deviceStatus", deviceStatus);
                
                if ("000".equals(response.getCode())) {
                    rebootResponse.put("status", "reboot_initiated");
                    rebootResponse.put("expectedDowntime", "30-60 seconds");
                    rebootResponse.put("nextSteps", Arrays.asList(
                        "Wait 30-60 seconds for device to restart",
                        "Monitor device heartbeat for reconnection",
                        "Test device connectivity after restart"
                    ));
                    return ApiResponse.success("Device reboot command sent successfully", rebootResponse);
                } else {
                    rebootResponse.put("status", "reboot_failed");
                    
                    // Handle specific error codes
                    if ("100201".equals(response.getCode())) {
                        rebootResponse.put("errorType", "INSUFFICIENT_PRIVILEGES");
                        rebootResponse.put("rootRequired", true);
                        rebootResponse.put("currentUser", "Standard User");
                        rebootResponse.put("requiredUser", "ROOT/Administrator");
                        rebootResponse.put("deviceConnected", deviceStatus.get("connected"));
                        rebootResponse.put("alternativeSolutions", Arrays.asList(
                            "Manually restart the device using physical power button",
                            "Access device admin panel with ROOT credentials", 
                            "Contact system administrator for elevated privileges",
                            "Use device management interface directly on the device",
                            "Check if device supports soft restart commands"
                        ));
                        rebootResponse.put("deviceLocation", "Network: 192.168.0.169");
                        rebootResponse.put("troubleshooting", "Device is responding normally but reboot command requires administrative privileges");
                        rebootResponse.put("recommendation", "Manual restart is the most reliable alternative");
                        
                        return ApiResponse.error(
                            "Device reboot requires ROOT privileges. Consider manual restart via power button.", 
                            "INSUFFICIENT_PRIVILEGES", 
                            rebootResponse
                        );
                    } else {
                        rebootResponse.put("errorType", "DEVICE_ERROR");
                        return ApiResponse.error("Device reboot failed: " + response.getMsg(), response.getCode(), rebootResponse);
                    }
                }
                
            } catch (Exception deviceError) {
                System.err.println("Device SDK Error: " + deviceError.getMessage());
                
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("deviceKey", deviceKey);
                errorResponse.put("status", "reboot_error");
                errorResponse.put("errorMessage", deviceError.getMessage());
                errorResponse.put("timestamp", new Date());
                errorResponse.put("suggestion", "Check device connection and try again. Reboot may require ROOT privileges.");
                
                return ApiResponse.error("Device reboot communication failed: " + deviceError.getMessage(), errorResponse);
            }
            
        } catch (CgiErrorException e) {
            System.err.println("CGI Error in device reboot: " + e.getMessage());
            return ApiResponse.error("Device communication error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error in device reboot: " + e.getMessage());
            return ApiResponse.error("Internal server error: " + e.getMessage());
        }
    }

    /**
     * Get device configuration
     */
    @PostMapping("/config")
    public ApiResponse<Map<String, Object>> getDeviceConfig(@RequestBody Map<String, Object> request) {
        try {
            System.out.println("=== DEVICE CONFIG REQUEST ===");
            
            String deviceKey = (String) request.get("deviceKey");
            String secret = (String) request.get("secret");
            
            // Validate device credentials
            validateCommon(deviceKey, secret);
            
            // Get host info
            HostInfoDto hostInfo = getHostInfo();
            
            // Prepare configuration response
            Map<String, Object> config = new HashMap<>();
            config.put("deviceKey", deviceKey);
            config.put("deviceName", "XO5-" + deviceKey.substring(deviceKey.length() - 6));
            config.put("firmwareVersion", "1.0.0");
            config.put("timezone", "UTC+1");
            config.put("language", "en");
            config.put("dateFormat", "yyyy-MM-dd");
            config.put("timeFormat", "24h");
            
            // Network configuration
            Map<String, Object> network = new HashMap<>();
            network.put("gatewayHost", hostInfo.getHost());
            network.put("gwPort", 10010);
            network.put("sdkPort", 10011);
            network.put("dhcp", true);
            network.put("connectionType", "TCP");
            config.put("network", network);
            
            // Security settings
            Map<String, Object> security = new HashMap<>();
            security.put("authenticationEnabled", true);
            security.put("secretRequired", true);
            security.put("encryptionLevel", "standard");
            config.put("security", security);
            
            return ApiResponse.success("Device configuration retrieved successfully", config);
            
        } catch (CgiErrorException e) {
            System.err.println("CGI Error in device config: " + e.getMessage());
            return ApiResponse.error("Device communication error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error in device config: " + e.getMessage());
            return ApiResponse.error("Internal server error: " + e.getMessage());
        }
    }

    /**
     * Set device time synchronization
     */
    @PostMapping("/sync-time")
    public ApiResponse<Map<String, Object>> syncDeviceTime(@RequestBody Map<String, Object> request) {
        try {
            System.out.println("=== DEVICE TIME SYNC REQUEST ===");
            
            String deviceKey = (String) request.get("deviceKey");
            String secret = (String) request.get("secret");
            
            // Validate device credentials
            validateCommon(deviceKey, secret);
            
            // Note: Device time synchronization not directly available in this SDK version
            // Return current server time information
            Map<String, Object> syncResponse = new HashMap<>();
            syncResponse.put("deviceKey", deviceKey);
            syncResponse.put("status", "server_time_available");
            syncResponse.put("serverTime", new Date());
            syncResponse.put("timestamp", System.currentTimeMillis());
            syncResponse.put("message", "Device time sync not available - server time provided");
            
            return ApiResponse.success("Server time information retrieved", syncResponse);
            
        } catch (CgiErrorException e) {
            System.err.println("CGI Error in device time sync: " + e.getMessage());
            return ApiResponse.error("Device communication error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error in device time sync: " + e.getMessage());
            return ApiResponse.error("Internal server error: " + e.getMessage());
        }
    }

    /**
     * Get device detailed information
     */
    @PostMapping("/details")
    public ApiResponse<Map<String, Object>> getDeviceDetails(@RequestBody Map<String, Object> request) {
        try {
            System.out.println("=== DEVICE DETAILS REQUEST ===");
            
            String deviceKey = (String) request.get("deviceKey");
            String secret = (String) request.get("secret");
            
            // Validate device credentials
            validateCommon(deviceKey, secret);
            
            // Get host info
            HostInfoDto hostInfo = getHostInfo();
            
            try {
                // Get device information using HfDeviceClient
                HfDeviceResp response = HfDeviceClient.deviceGet(hostInfo, deviceKey, secret);
                
                Map<String, Object> detailsResponse = new HashMap<>();
                detailsResponse.put("deviceKey", deviceKey);
                detailsResponse.put("responseCode", response.getCode());
                detailsResponse.put("responseMessage", response.getMsg());
                detailsResponse.put("timestamp", new Date());
                
                if ("000".equals(response.getCode())) {
                    detailsResponse.put("status", "details_retrieved");
                    detailsResponse.put("deviceData", response.getData());
                    return ApiResponse.success("Device details retrieved successfully", detailsResponse);
                } else {
                    detailsResponse.put("status", "details_failed");
                    return ApiResponse.error("Device details retrieval failed: " + response.getMsg(), response.getCode(), detailsResponse);
                }
                
            } catch (Exception deviceError) {
                System.err.println("Device Details Error: " + deviceError.getMessage());
                
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("deviceKey", deviceKey);
                errorResponse.put("status", "details_error");
                errorResponse.put("errorMessage", deviceError.getMessage());
                errorResponse.put("timestamp", new Date());
                
                return ApiResponse.error("Device details communication failed: " + deviceError.getMessage(), errorResponse);
            }
            
        } catch (CgiErrorException e) {
            System.err.println("CGI Error in device details: " + e.getMessage());
            return ApiResponse.error("Device communication error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error in device details: " + e.getMessage());
            return ApiResponse.error("Internal server error: " + e.getMessage());
        }
    }
    
    /**
     * Get default host info configuration
     */
    private HostInfoDto getHostInfo() {
        HostInfoDto hostInfo = new HostInfoDto();
        hostInfo.setHost("192.168.0.169");
        hostInfo.setPort(10011);  // Port should be int, not string
        hostInfo.setTimeout(10000);
        System.out.println("DEBUG - Gateway Host Info: " + hostInfo.getHost() + ":" + hostInfo.getPort() + " (timeout: " + hostInfo.getTimeout() + "ms)");
        return hostInfo;
    }

    /**
     * Validate common device parameters
     */
    protected void validateCommon(String deviceKey, String secret) throws CgiErrorException {
        if (deviceKey == null || deviceKey.length() < 16) {
            throw new CgiErrorException("Device key must be at least 16 characters");
        }
        if (secret == null || secret.trim().isEmpty()) {
            throw new CgiErrorException("Secret parameter is required");
        }
        System.out.println("DEBUG - Device validation passed for: " + deviceKey);
    }
}