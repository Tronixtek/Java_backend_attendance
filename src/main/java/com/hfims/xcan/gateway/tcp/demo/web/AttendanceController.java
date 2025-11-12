package com.hfims.xcan.gateway.tcp.demo.web;

import com.hfims.xcan.gateway.netty.client.HfDeviceClient;
import com.hfims.xcan.gateway.netty.client.resp.HfDeviceResp;
import com.hfims.xcan.gateway.tcp.demo.support.BaseResult;
import com.hfims.xcan.gateway.tcp.demo.support.ResultWrapper;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/attendance")
@CrossOrigin(origins = "*")
public class AttendanceController extends BaseController {

    /**
     * Get attendance records from device
     */
    @PostMapping("/records")
    public BaseResult getAttendanceRecords(@RequestBody AttendanceRecordsRequest request) {
        System.out.println("=== GET ATTENDANCE RECORDS REQUEST ===");
        System.out.println("Device Key: " + request.getDeviceKey());
        System.out.println("Employee ID: " + request.getEmployeeId());
        System.out.println("Start Date: " + request.getStartDate());
        System.out.println("End Date: " + request.getEndDate());

        try {
            // Test device connectivity
            HfDeviceResp testResponse = HfDeviceClient.test(hostInfo, request.getDeviceKey(), request.getSecret());
            if (!"000".equals(testResponse.getCode())) {
                return ResultWrapper.wrapFailure("1002", "Device connectivity failed: " + testResponse.getMsg());
            }

            // Get attendance records from device
            HfDeviceResp recordsResponse = getRecordsFromDevice(request);
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("deviceConnected", true);
            
            if ("000".equals(recordsResponse.getCode()) && recordsResponse.getData() != null) {
                System.out.println("✅ Successfully retrieved attendance records");
                
                // Parse attendance records from device response
                List<Map<String, Object>> attendanceRecords = parseAttendanceRecords(recordsResponse.getData());
                
                // Filter records if specific employee requested
                if (request.getEmployeeId() != null && !request.getEmployeeId().isEmpty()) {
                    attendanceRecords = filterRecordsByEmployee(attendanceRecords, request.getEmployeeId());
                }
                
                // Filter records by date range if specified
                if (request.getStartDate() != null || request.getEndDate() != null) {
                    attendanceRecords = filterRecordsByDateRange(attendanceRecords, request.getStartDate(), request.getEndDate());
                }
                
                responseData.put("success", true);
                responseData.put("message", "Attendance records retrieved successfully");
                responseData.put("totalRecords", attendanceRecords.size());
                responseData.put("attendanceRecords", attendanceRecords);
                responseData.put("deviceResponse", recordsResponse.getMsg());
                
                // Add summary statistics
                Map<String, Object> statistics = generateAttendanceStatistics(attendanceRecords);
                responseData.put("statistics", statistics);
                
                return ResultWrapper.wrapSuccess(responseData);
                
            } else {
                System.out.println("⚠️ No attendance records found or device error");
                responseData.put("success", false);
                responseData.put("message", "No attendance records found");
                responseData.put("totalRecords", 0);
                responseData.put("attendanceRecords", new ArrayList<>());
                responseData.put("deviceResponse", recordsResponse.getMsg());
                
                return ResultWrapper.wrapSuccess(responseData);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResultWrapper.wrapFailure("1000", "Failed to retrieve attendance records: " + e.getMessage());
        }
    }

    /**
     * Get specific attendance record by ID
     */
    @PostMapping("/record")
    public BaseResult getAttendanceRecord(@RequestBody AttendanceRecordRequest request) {
        System.out.println("=== GET SPECIFIC ATTENDANCE RECORD REQUEST ===");
        System.out.println("Device Key: " + request.getDeviceKey());
        System.out.println("Record ID: " + request.getRecordId());

        try {
            // Test device connectivity
            HfDeviceResp testResponse = HfDeviceClient.test(hostInfo, request.getDeviceKey(), request.getSecret());
            if (!"000".equals(testResponse.getCode())) {
                return ResultWrapper.wrapFailure("1002", "Device connectivity failed: " + testResponse.getMsg());
            }

            // Find specific record
            HfDeviceResp recordResponse = findSpecificRecord(request);
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("deviceConnected", true);
            responseData.put("recordId", request.getRecordId());
            
            if ("000".equals(recordResponse.getCode()) && recordResponse.getData() != null) {
                System.out.println("✅ Found attendance record: " + request.getRecordId());
                
                Map<String, Object> attendanceRecord = parseAttendanceRecord(recordResponse.getData());
                
                responseData.put("found", true);
                responseData.put("message", "Attendance record found successfully");
                responseData.put("attendanceRecord", attendanceRecord);
                responseData.put("deviceResponse", recordResponse.getMsg());
                
                return ResultWrapper.wrapSuccess(responseData);
                
            } else {
                System.out.println("⚠️ Attendance record not found: " + request.getRecordId());
                responseData.put("found", false);
                responseData.put("message", "Attendance record not found");
                responseData.put("attendanceRecord", null);
                responseData.put("deviceResponse", recordResponse.getMsg());
                
                return ResultWrapper.wrapSuccess(responseData);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResultWrapper.wrapFailure("1000", "Failed to retrieve attendance record: " + e.getMessage());
        }
    }

    /**
     * Get real-time attendance events and device status
     */
    @PostMapping("/monitor")
    public BaseResult monitorAttendance(@RequestBody AttendanceMonitorRequest request) {
        System.out.println("=== MONITOR ATTENDANCE REQUEST ===");
        System.out.println("Device Key: " + request.getDeviceKey());

        try {
            // Test device connectivity
            HfDeviceResp testResponse = HfDeviceClient.test(hostInfo, request.getDeviceKey(), request.getSecret());
            if (!"000".equals(testResponse.getCode())) {
                return ResultWrapper.wrapFailure("1002", "Device connectivity failed: " + testResponse.getMsg());
            }

            // Get device status and latest events
            HfDeviceResp deviceStatus = getDeviceStatus(request);
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("deviceConnected", true);
            responseData.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            if ("000".equals(deviceStatus.getCode())) {
                System.out.println("✅ Device monitoring active");
                
                responseData.put("deviceOnline", true);
                responseData.put("message", "Device is online and monitoring attendance");
                responseData.put("deviceInfo", parseDeviceInfo(deviceStatus.getData()));
                
                // Get recent attendance events (last 24 hours)
                AttendanceRecordsRequest recentRecords = new AttendanceRecordsRequest();
                recentRecords.setDeviceKey(request.getDeviceKey());
                recentRecords.setSecret(request.getSecret());
                recentRecords.setStartDate(LocalDate.now().minusDays(1).toString());
                
                try {
                    HfDeviceResp recentResponse = getRecordsFromDevice(recentRecords);
                    if ("000".equals(recentResponse.getCode()) && recentResponse.getData() != null) {
                        List<Map<String, Object>> recentEvents = parseAttendanceRecords(recentResponse.getData());
                        responseData.put("recentEvents", recentEvents);
                        responseData.put("recentEventCount", recentEvents.size());
                    } else {
                        responseData.put("recentEvents", new ArrayList<>());
                        responseData.put("recentEventCount", 0);
                    }
                } catch (Exception e) {
                    System.out.println("Could not retrieve recent events: " + e.getMessage());
                    responseData.put("recentEvents", new ArrayList<>());
                    responseData.put("recentEventCount", 0);
                }
                
                return ResultWrapper.wrapSuccess(responseData);
                
            } else {
                System.out.println("⚠️ Device monitoring failed");
                responseData.put("deviceOnline", false);
                responseData.put("message", "Device is offline or not responding");
                responseData.put("deviceResponse", deviceStatus.getMsg());
                
                return ResultWrapper.wrapSuccess(responseData);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResultWrapper.wrapFailure("1000", "Failed to monitor attendance: " + e.getMessage());
        }
    }

    /**
     * Get attendance statistics and reports
     */
    @PostMapping("/statistics")
    public BaseResult getAttendanceStatistics(@RequestBody AttendanceStatisticsRequest request) {
        System.out.println("=== GET ATTENDANCE STATISTICS REQUEST ===");
        System.out.println("Device Key: " + request.getDeviceKey());
        System.out.println("Employee ID: " + request.getEmployeeId());
        System.out.println("Start Date: " + request.getStartDate());
        System.out.println("End Date: " + request.getEndDate());

        try {
            // Test device connectivity
            HfDeviceResp testResponse = HfDeviceClient.test(hostInfo, request.getDeviceKey(), request.getSecret());
            if (!"000".equals(testResponse.getCode())) {
                return ResultWrapper.wrapFailure("1002", "Device connectivity failed: " + testResponse.getMsg());
            }

            // Get attendance records for analysis
            AttendanceRecordsRequest recordsRequest = new AttendanceRecordsRequest();
            recordsRequest.setDeviceKey(request.getDeviceKey());
            recordsRequest.setSecret(request.getSecret());
            recordsRequest.setEmployeeId(request.getEmployeeId());
            recordsRequest.setStartDate(request.getStartDate());
            recordsRequest.setEndDate(request.getEndDate());
            
            HfDeviceResp recordsResponse = getRecordsFromDevice(recordsRequest);
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("deviceConnected", true);
            
            if ("000".equals(recordsResponse.getCode()) && recordsResponse.getData() != null) {
                System.out.println("✅ Successfully retrieved records for statistics");
                
                List<Map<String, Object>> attendanceRecords = parseAttendanceRecords(recordsResponse.getData());
                
                // Filter records if specific employee requested
                if (request.getEmployeeId() != null && !request.getEmployeeId().isEmpty()) {
                    attendanceRecords = filterRecordsByEmployee(attendanceRecords, request.getEmployeeId());
                }
                
                // Filter records by date range
                if (request.getStartDate() != null || request.getEndDate() != null) {
                    attendanceRecords = filterRecordsByDateRange(attendanceRecords, request.getStartDate(), request.getEndDate());
                }
                
                // Generate comprehensive statistics
                Map<String, Object> statistics = generateDetailedStatistics(attendanceRecords, request);
                
                responseData.put("success", true);
                responseData.put("message", "Attendance statistics generated successfully");
                responseData.put("totalRecords", attendanceRecords.size());
                responseData.put("statistics", statistics);
                responseData.put("generatedAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                
                return ResultWrapper.wrapSuccess(responseData);
                
            } else {
                System.out.println("⚠️ No records found for statistics");
                responseData.put("success", false);
                responseData.put("message", "No attendance records found for the specified criteria");
                responseData.put("totalRecords", 0);
                responseData.put("statistics", generateEmptyStatistics());
                
                return ResultWrapper.wrapSuccess(responseData);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResultWrapper.wrapFailure("1000", "Failed to generate attendance statistics: " + e.getMessage());
        }
    }

    // ==================== HELPER METHODS ====================

    /**
     * Get attendance records from device using recordFindList
     */
    private HfDeviceResp getRecordsFromDevice(AttendanceRecordsRequest request) throws Exception {
        try {
            System.out.println("=== ATTEMPTING RECORDS RETRIEVAL ===");
            
            // Build RecordFindListReq using reflection
            Class<?> recordFindListReqClass = Class.forName("com.hfims.xcan.gateway.netty.client.req.RecordFindListReq");
            Object recordFindListReq = recordFindListReqClass.getDeclaredConstructor().newInstance();
            
            System.out.println("✅ RecordFindListReq object created successfully");
            
            // Set optional filters if provided
            // Note: The exact field names may vary - we'll try common ones
            if (request.getEmployeeId() != null && !request.getEmployeeId().isEmpty()) {
                try {
                    Method setSnMethod = recordFindListReqClass.getMethod("setSn", String.class);
                    setSnMethod.invoke(recordFindListReq, request.getEmployeeId());
                    System.out.println("Set employee filter: " + request.getEmployeeId());
                } catch (NoSuchMethodException e) {
                    System.out.println("Employee filter not supported by RecordFindListReq");
                }
            }
            
            // Get the method signature for recordFindList
            Class<?> hostInfoClass = Class.forName("com.hfims.xcan.gateway.netty.client.dto.HostInfoDto");
            
            Method recordFindListMethod = HfDeviceClient.class.getMethod("recordFindList",
                hostInfoClass, String.class, String.class, recordFindListReqClass);
            
            System.out.println("✅ Found recordFindList method in HfDeviceClient");
            
            // Call the recordFindList method
            HfDeviceResp response = (HfDeviceResp) recordFindListMethod.invoke(null, hostInfo, request.getDeviceKey(), request.getSecret(), recordFindListReq);
            
            System.out.println("RecordFindList response - Code: " + response.getCode() + ", Message: " + response.getMsg());
            return response;
            
        } catch (Exception e) {
            System.err.println("Failed to get records from device: " + e.getMessage());
            e.printStackTrace();
            throw new Exception("Failed to retrieve attendance records: " + e.getMessage());
        }
    }

    /**
     * Find specific attendance record using recordFind
     */
    private HfDeviceResp findSpecificRecord(AttendanceRecordRequest request) throws Exception {
        try {
            System.out.println("=== ATTEMPTING SPECIFIC RECORD FIND ===");
            
            // Build RecordFindReq using reflection
            Class<?> recordFindReqClass = Class.forName("com.hfims.xcan.gateway.netty.client.req.RecordFindReq");
            Object recordFindReq = recordFindReqClass.getDeclaredConstructor().newInstance();
            
            System.out.println("✅ RecordFindReq object created successfully");
            
            // Try to set the record ID
            boolean fieldSet = false;
            String[] fieldNames = {"id", "recordId", "sn", "index"};
            
            for (String fieldName : fieldNames) {
                try {
                    String methodName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
                    Method setMethod = recordFindReqClass.getMethod(methodName, String.class);
                    setMethod.invoke(recordFindReq, request.getRecordId());
                    System.out.println("✅ Set field '" + fieldName + "' to: " + request.getRecordId());
                    fieldSet = true;
                    break;
                } catch (NoSuchMethodException e) {
                    System.out.println("Method " + fieldName + " not found, trying next...");
                }
            }
            
            if (!fieldSet) {
                throw new Exception("Could not find appropriate field setter for record ID in RecordFindReq");
            }
            
            // Get the method signature for recordFind
            Class<?> hostInfoClass = Class.forName("com.hfims.xcan.gateway.netty.client.dto.HostInfoDto");
            
            Method recordFindMethod = HfDeviceClient.class.getMethod("recordFind",
                hostInfoClass, String.class, String.class, recordFindReqClass);
            
            System.out.println("✅ Found recordFind method in HfDeviceClient");
            
            // Call the recordFind method
            HfDeviceResp response = (HfDeviceResp) recordFindMethod.invoke(null, hostInfo, request.getDeviceKey(), request.getSecret(), recordFindReq);
            
            System.out.println("RecordFind response - Code: " + response.getCode() + ", Message: " + response.getMsg());
            return response;
            
        } catch (Exception e) {
            System.err.println("Failed to find specific record: " + e.getMessage());
            e.printStackTrace();
            throw new Exception("Failed to find attendance record: " + e.getMessage());
        }
    }

    /**
     * Get device status for monitoring
     */
    private HfDeviceResp getDeviceStatus(AttendanceMonitorRequest request) throws Exception {
        try {
            // Use deviceGet method to get device status
            HfDeviceResp response = HfDeviceClient.deviceGet(hostInfo, request.getDeviceKey(), request.getSecret());
            System.out.println("Device status response - Code: " + response.getCode() + ", Message: " + response.getMsg());
            return response;
        } catch (Exception e) {
            System.err.println("Failed to get device status: " + e.getMessage());
            throw new Exception("Failed to get device status: " + e.getMessage());
        }
    }

    /**
     * Parse attendance records from device response
     */
    private List<Map<String, Object>> parseAttendanceRecords(Object data) {
        List<Map<String, Object>> attendanceRecords = new ArrayList<>();
        
        try {
            List<?> recordList = null;
            
            if (data instanceof Map) {
                Map<?, ?> dataMap = (Map<?, ?>) data;
                recordList = (List<?>) dataMap.get("data");
                if (recordList == null) recordList = (List<?>) dataMap.get("list");
                if (recordList == null) recordList = (List<?>) dataMap.get("records");
            } else if (data instanceof List) {
                recordList = (List<?>) data;
            }
            
            if (recordList != null) {
                for (Object record : recordList) {
                    if (record instanceof Map) {
                        Map<?, ?> recordMap = (Map<?, ?>) record;
                        Map<String, Object> attendanceRecord = parseAttendanceRecord(recordMap);
                        attendanceRecords.add(attendanceRecord);
                    }
                }
            }
            
            System.out.println("Parsed " + attendanceRecords.size() + " attendance records");
            
        } catch (Exception e) {
            System.err.println("Error parsing attendance records: " + e.getMessage());
        }
        
        return attendanceRecords;
    }

    /**
     * Parse single attendance record
     */
    private Map<String, Object> parseAttendanceRecord(Object recordData) {
        Map<String, Object> attendanceRecord = new HashMap<>();
        
        try {
            if (recordData instanceof Map) {
                Map<?, ?> recordMap = (Map<?, ?>) recordData;
                
                // DEBUG: Print all available keys in the record
                System.out.println("DEBUG - Available keys in record: " + recordMap.keySet());
                System.out.println("DEBUG - Full record data: " + recordMap);
                
                // Extract common fields with correct XO5 field names
                attendanceRecord.put("recordId", getFieldValue(recordMap, "id", "recordId", "index", "Id"));
                attendanceRecord.put("employeeId", getFieldValue(recordMap, "sn", "personSn", "employeeId", "Sn", "PersonSn"));
                attendanceRecord.put("employeeName", getFieldValue(recordMap, "name", "personName", "userName", "Name", "PersonName"));
                // Fix: Use actual XO5 field names
                attendanceRecord.put("timestamp", getFieldValue(recordMap, "createTime", "time", "recordTime", "dateTime", "Time", "RecordTime", "DateTime"));
                attendanceRecord.put("eventType", getFieldValue(recordMap, "resultFlag", "type", "eventType", "recordType", "Type", "EventType", "RecordType"));
                attendanceRecord.put("verifyMode", getFieldValue(recordMap, "fingerFlag", "faceFlag", "verifyMode", "mode", "authMode", "VerifyMode", "Mode", "AuthMode"));
                attendanceRecord.put("deviceKey", getFieldValue(recordMap, "deviceKey", "device", "DeviceKey", "Device"));
                attendanceRecord.put("temperature", getFieldValue(recordMap, "temperature", "temp", "Temperature", "Temp"));
                attendanceRecord.put("photo", getFieldValue(recordMap, "checkImgUrl", "photo", "image", "Photo", "Image"));
                attendanceRecord.put("direction", getFieldValue(recordMap, "direction", "Direction"));
                attendanceRecord.put("strangerFlag", getFieldValue(recordMap, "strangerFlag", "StrangerFlag"));
                attendanceRecord.put("personType", getFieldValue(recordMap, "personType", "PersonType"));
                
                // Debug output for field extraction
                System.out.println("DEBUG - Extracted timestamp: " + attendanceRecord.get("timestamp"));
                System.out.println("DEBUG - Extracted eventType: " + attendanceRecord.get("eventType"));
                System.out.println("DEBUG - Extracted verifyMode: " + attendanceRecord.get("verifyMode"));
                
                // Format timestamp if available
                Object timestamp = attendanceRecord.get("timestamp");
                if (timestamp != null) {
                    attendanceRecord.put("formattedTime", formatTimestamp(timestamp));
                }
                
                // Determine check-in/check-out type
                Object eventType = attendanceRecord.get("eventType");
                if (eventType != null) {
                    attendanceRecord.put("attendanceType", determineAttendanceType(eventType));
                }
                
                System.out.println("Parsed attendance record: Employee " + attendanceRecord.get("employeeId") + 
                    " at " + attendanceRecord.get("formattedTime"));
            }
            
        } catch (Exception e) {
            System.err.println("Error parsing attendance record: " + e.getMessage());
        }
        
        return attendanceRecord;
    }

    // Additional helper methods continue...
    
    private Object getFieldValue(Map<?, ?> map, String... fieldNames) {
        for (String fieldName : fieldNames) {
            Object value = map.get(fieldName);
            if (value != null) {
                return value;
            }
        }
        return null;
    }
    
    private String formatTimestamp(Object timestamp) {
        try {
            if (timestamp instanceof Number) {
                // XO5 device sends timestamp in milliseconds since epoch
                long ts = ((Number) timestamp).longValue();
                
                // Handle XO5 timestamp format (already in milliseconds)
                if (ts > 1000000000000L) { // If timestamp is in milliseconds
                    return LocalDateTime.ofInstant(
                        java.time.Instant.ofEpochMilli(ts), 
                        java.time.ZoneId.systemDefault()
                    ).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                } else { // If timestamp is in seconds, convert to milliseconds
                    ts = ts * 1000;
                    return LocalDateTime.ofInstant(
                        java.time.Instant.ofEpochMilli(ts), 
                        java.time.ZoneId.systemDefault()
                    ).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                }
            }
            return timestamp.toString();
        } catch (Exception e) {
            System.err.println("Error formatting timestamp: " + e.getMessage());
            return timestamp.toString();
        }
    }
    
    private String determineAttendanceType(Object eventType) {
        if (eventType == null) return "UNKNOWN";
        
        String type = eventType.toString().toLowerCase();
        if (type.contains("in") || type.equals("0")) {
            return "CHECK_IN";
        } else if (type.contains("out") || type.equals("1")) {
            return "CHECK_OUT";
        } else {
            return "UNKNOWN";
        }
    }
    
    private List<Map<String, Object>> filterRecordsByEmployee(List<Map<String, Object>> records, String employeeId) {
        return records.stream()
            .filter(record -> employeeId.equals(record.get("employeeId")))
            .collect(java.util.stream.Collectors.toList());
    }
    
    private List<Map<String, Object>> filterRecordsByDateRange(List<Map<String, Object>> records, String startDate, String endDate) {
        // Simple date filtering - can be enhanced based on timestamp format
        return records; // For now, return all records
    }
    
    private Map<String, Object> generateAttendanceStatistics(List<Map<String, Object>> records) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRecords", records.size());
        stats.put("checkIns", records.stream().mapToInt(r -> "CHECK_IN".equals(r.get("attendanceType")) ? 1 : 0).sum());
        stats.put("checkOuts", records.stream().mapToInt(r -> "CHECK_OUT".equals(r.get("attendanceType")) ? 1 : 0).sum());
        stats.put("uniqueEmployees", records.stream().map(r -> r.get("employeeId")).distinct().count());
        return stats;
    }
    
    private Map<String, Object> generateDetailedStatistics(List<Map<String, Object>> records, AttendanceStatisticsRequest request) {
        // Enhanced statistics with more detailed analysis
        return generateAttendanceStatistics(records);
    }
    
    private Map<String, Object> generateEmptyStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRecords", 0);
        stats.put("checkIns", 0);
        stats.put("checkOuts", 0);
        stats.put("uniqueEmployees", 0);
        return stats;
    }
    
    private Map<String, Object> parseDeviceInfo(Object data) {
        Map<String, Object> deviceInfo = new HashMap<>();
        
        if (data instanceof Map) {
            Map<?, ?> dataMap = (Map<?, ?>) data;
            deviceInfo.put("deviceModel", dataMap.get("model"));
            deviceInfo.put("firmwareVersion", dataMap.get("version"));
            deviceInfo.put("serialNumber", dataMap.get("sn"));
            deviceInfo.put("status", "online");
        }
        
        return deviceInfo;
    }
}

// ==================== REQUEST CLASSES ====================

/**
 * Request class for attendance records
 */
class AttendanceRecordsRequest {
    private String deviceKey;
    private String secret;
    private String employeeId; // Optional filter
    private String startDate;  // Optional filter (YYYY-MM-DD)
    private String endDate;    // Optional filter (YYYY-MM-DD)
    private Integer pageSize;  // Optional pagination
    private Integer pageNumber; // Optional pagination

    // Getters and setters
    public String getDeviceKey() { return deviceKey; }
    public void setDeviceKey(String deviceKey) { this.deviceKey = deviceKey; }
    
    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }
    
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    
    public Integer getPageSize() { return pageSize; }
    public void setPageSize(Integer pageSize) { this.pageSize = pageSize; }
    
    public Integer getPageNumber() { return pageNumber; }
    public void setPageNumber(Integer pageNumber) { this.pageNumber = pageNumber; }
}

/**
 * Request class for specific attendance record
 */
class AttendanceRecordRequest {
    private String deviceKey;
    private String secret;
    private String recordId;

    public String getDeviceKey() { return deviceKey; }
    public void setDeviceKey(String deviceKey) { this.deviceKey = deviceKey; }
    
    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }
    
    public String getRecordId() { return recordId; }
    public void setRecordId(String recordId) { this.recordId = recordId; }
}

/**
 * Request class for attendance statistics
 */
class AttendanceStatisticsRequest {
    private String deviceKey;
    private String secret;
    private String employeeId; // Optional filter
    private String startDate;  // Optional filter
    private String endDate;    // Optional filter
    private String reportType; // daily, weekly, monthly

    public String getDeviceKey() { return deviceKey; }
    public void setDeviceKey(String deviceKey) { this.deviceKey = deviceKey; }
    
    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }
    
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    
    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }
}

/**
 * Request class for attendance monitoring
 */
class AttendanceMonitorRequest {
    private String deviceKey;
    private String secret;

    public String getDeviceKey() { return deviceKey; }
    public void setDeviceKey(String deviceKey) { this.deviceKey = deviceKey; }
    
    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }
}