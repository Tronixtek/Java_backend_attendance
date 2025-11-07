package com.hfims.xcan.gateway.tcp.demo.web;

import com.hfims.xcan.gateway.netty.client.HfDeviceClient;
import com.hfims.xcan.gateway.netty.client.resp.HfDeviceResp;
import com.hfims.xcan.gateway.netty.error.CgiErrorException;
import com.hfims.xcan.gateway.tcp.demo.support.BaseResult;
import com.hfims.xcan.gateway.tcp.demo.support.ResultWrapper;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api")
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class TestController extends BaseController {

    // DTO class for JSON request body
    public static class DeviceRequest {
        private String deviceKey;
        private String secret;

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
    }

    // Basic health check endpoint (no parameters required)
    @GetMapping("/health")
    public BaseResult health() {
        return ResultWrapper.wrapSuccess();
    }

    // Server info endpoint for debugging cloud deployment
    @GetMapping("/info")
    public BaseResult serverInfo() {
        try {
            String serverHost = hostInfo != null ? hostInfo.getHost() : "unknown";
            int serverPort = hostInfo != null ? hostInfo.getPort() : 0;
            
            return ResultWrapper.wrapSuccess(
                "Gateway Host: " + serverHost + 
                ", SDK Port: " + serverPort + 
                ", API Server: Cloud Ready"
            );
        } catch (Exception e) {
            return ResultWrapper.wrapFailure("1001", "Server info error: " + e.getMessage());
        }
    }

    // Device test endpoint (supports both JSON and form parameters)
    @PostMapping("/test")
    public BaseResult test(@RequestBody(required = false) DeviceRequest jsonRequest,
                          @RequestParam(value = "deviceKey", required = false) String formDeviceKey,
                          @RequestParam(value = "secret", required = false) String formSecret) {
        
        // Handle both JSON and form parameters
        String deviceKey = jsonRequest != null ? jsonRequest.getDeviceKey() : formDeviceKey;
        String secret = jsonRequest != null ? jsonRequest.getSecret() : formSecret;
        
        // Cloud deployment logging
        System.out.println("CLOUD - Device test request from: " + 
            (jsonRequest != null ? "JSON" : "Form") + " parameters");
        System.out.println("CLOUD - DeviceKey: " + (deviceKey != null ? deviceKey.substring(0, Math.min(8, deviceKey.length())) + "..." : "null"));
            
        try {
            validateCommon(deviceKey, secret);
            System.out.println("DEBUG - Calling HfDeviceClient.test with hostInfo: " + hostInfo.getHost() + ":" + hostInfo.getPort());
            HfDeviceResp tdxSdkResp = HfDeviceClient.test(hostInfo, deviceKey, secret);
            System.out.println("DEBUG - HfDeviceClient.test response: " + tdxSdkResp);
            return ResultWrapper.wrapTdxSdkResponse(tdxSdkResp);
        } catch (CgiErrorException e) {
            System.out.println("DEBUG - CgiErrorException: " + e.getMessage());
            return ResultWrapper.wrapFailure("1000", "Validation failed: deviceKey must be at least 16 characters and secret cannot be empty");
        } catch (Exception e) {
            System.out.println("CLOUD - Device communication error: " + e.getMessage());
            e.printStackTrace();
            return ResultWrapper.wrapFailure("1002", "Device communication error: " + e.getMessage());
        }
    }

    @PostMapping("/get")
    public BaseResult get(@RequestBody(required = false) DeviceRequest jsonRequest,
                         @RequestParam(value = "deviceKey", required = false) String formDeviceKey,
                         @RequestParam(value = "secret", required = false) String formSecret) {
        
        // Handle both JSON and form parameters
        String deviceKey = jsonRequest != null ? jsonRequest.getDeviceKey() : formDeviceKey;
        String secret = jsonRequest != null ? jsonRequest.getSecret() : formSecret;
        
        // Debug logging to see what parameters are received
        System.out.println("DEBUG - Received deviceKey: '" + deviceKey + "' (length: " + 
            (deviceKey != null ? deviceKey.length() : "null") + ")");
        System.out.println("DEBUG - Received secret: '" + secret + "' (empty: " + 
            (secret == null || secret.isEmpty()) + ")");
        
        try {
            validateCommon(deviceKey, secret);
            HfDeviceResp tdxSdkResp = HfDeviceClient.deviceGet(hostInfo, deviceKey, secret);
            return ResultWrapper.wrapTdxSdkResponse(tdxSdkResp);
        } catch (CgiErrorException e) {
            return ResultWrapper.wrapFailure("1000", "Validation failed: deviceKey must be at least 16 characters and secret cannot be empty");
        } catch (Exception e) {
            return ResultWrapper.wrapFailure("1002", "Device communication error: " + e.getMessage());
        }
    }

    @PostMapping("/reboot")
    public BaseResult reboot(@RequestBody(required = false) DeviceRequest jsonRequest,
                            @RequestParam(value = "deviceKey", required = false) String formDeviceKey,
                            @RequestParam(value = "secret", required = false) String formSecret) {
        
        // Handle both JSON and form parameters
        String deviceKey = jsonRequest != null ? jsonRequest.getDeviceKey() : formDeviceKey;
        String secret = jsonRequest != null ? jsonRequest.getSecret() : formSecret;
        
        try {
            validateCommon(deviceKey, secret);
            HfDeviceResp tdxSdkResp = HfDeviceClient.deviceReboot(hostInfo, deviceKey, secret);
            return ResultWrapper.wrapTdxSdkResponse(tdxSdkResp);
        } catch (CgiErrorException e) {
            return ResultWrapper.wrapFailure("1000", "Validation failed: deviceKey must be at least 16 characters and secret cannot be empty");
        } catch (Exception e) {
            return ResultWrapper.wrapFailure("1002", "Device communication error: " + e.getMessage());
        }
    }

    // Additional endpoint for device status monitoring (useful for dashboard)
    @GetMapping("/status")
    public BaseResult getDeviceStatus(@RequestParam String deviceKey, @RequestParam String secret) {
        try {
            validateCommon(deviceKey, secret);
            HfDeviceResp response = HfDeviceClient.deviceGet(hostInfo, deviceKey, secret);
            
            // Create a simplified status response for dashboard
            boolean isConnected = "0000".equals(response.getCode());
            String status = isConnected ? "Connected" : "Disconnected";
            
            return ResultWrapper.wrapSuccess(java.util.Map.of(
                "deviceKey", deviceKey,
                "connected", isConnected,
                "status", status,
                "timestamp", System.currentTimeMillis(),
                "responseCode", response.getCode(),
                "responseMessage", response.getMsg()
            ));
        } catch (Exception e) {
            return ResultWrapper.wrapSuccess(java.util.Map.of(
                "deviceKey", deviceKey,
                "connected", false,
                "status", "Error: " + e.getMessage(),
                "timestamp", System.currentTimeMillis()
            ));
        }
    }
}
