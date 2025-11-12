package com.hfims.xcan.gateway.tcp.demo.service;

import org.springframework.stereotype.Service;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Service for building XO5 device request objects using reflection
 */
@Service
public class RequestBuilderService {

    private static final String PERSON_CREATE_REQ_CLASS = "com.hfims.xcan.gateway.netty.client.req.PersonCreateReq";
    private static final String PERSON_DELETE_REQ_CLASS = "com.hfims.xcan.gateway.netty.client.req.PersonDeleteReq";

    /**
     * Build PersonCreateReq object for employee sync
     */
    public Object buildPersonCreateReq(String employeeId, String fullName, String faceImage) {
        return buildPersonCreateReq(employeeId, fullName, faceImage, null);
    }

    /**
     * Build PersonCreateReq object for employee sync with verification style
     */
    public Object buildPersonCreateReq(String employeeId, String fullName, String faceImage, Integer verificationStyle) {
        try {
            System.out.println("=== BUILDING PERSON CREATE REQUEST ===");
            System.out.println("Employee ID: " + employeeId);
            System.out.println("Full Name: " + fullName);
            System.out.println("Has Face Image: " + (faceImage != null && !faceImage.isEmpty()));
            System.out.println("Verification Style: " + verificationStyle);
            
            // Load the PersonCreateReq class
            Class<?> personCreateReqClass = Class.forName(PERSON_CREATE_REQ_CLASS);
            Object personCreateReq = personCreateReqClass.getDeclaredConstructor().newInstance();
            
            // Get all methods for inspection
            Method[] methods = personCreateReqClass.getDeclaredMethods();
            Map<String, Method> methodMap = new HashMap<>();
            
            for (Method method : methods) {
                methodMap.put(method.getName().toLowerCase(), method);
            }
            
            // Set employee ID (sn field)
            setMethodValue(personCreateReq, methodMap, "setsn", employeeId, "setSn");
            
            // Set employee name
            setMethodValue(personCreateReq, methodMap, "setname", fullName, "setName");
            
            // Set type (1 = normal employee)
            setMethodValue(personCreateReq, methodMap, "settype", 1, "setType");
            
            // Set verify style - use provided value or default based on face image
            int verifyStyle;
            if (verificationStyle != null) {
                verifyStyle = verificationStyle;
                System.out.println("Using provided verification style: " + verifyStyle);
            } else {
                // Default behavior: 8 = face + password, 1 = face only, 3 = face
                verifyStyle = (faceImage != null && !faceImage.isEmpty()) ? 3 : 1;
                System.out.println("Using default verification style: " + verifyStyle);
            }
            setMethodValue(personCreateReq, methodMap, "setverifystyle", verifyStyle, "setVerifyStyle");
            
            // Try to set face image if provided
            if (faceImage != null && !faceImage.isEmpty()) {
                boolean faceSet = setMethodValue(personCreateReq, methodMap, "setfaceimage", faceImage, "setFaceImage") ||
                                 setMethodValue(personCreateReq, methodMap, "setface", faceImage, "setFace") ||
                                 setMethodValue(personCreateReq, methodMap, "setpersonface", faceImage, "setPersonFace");
                                 
                if (!faceSet) {
                    System.out.println("WARNING: Could not set face image using any of the method names: setFaceImage, setFace, setPersonFace");
                }
            }
            
            System.out.println("=== PERSON CREATE REQUEST BUILT SUCCESSFULLY ===");
            return personCreateReq;
            
        } catch (Exception e) {
            System.err.println("ERROR: Failed to build PersonCreateReq: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to build person create request: " + e.getMessage(), e);
        }
    }

    /**
     * Build PersonDeleteReq object for employee removal
     */
    public Object buildPersonDeleteReq(String employeeId) {
        try {
            System.out.println("=== BUILDING PERSON DELETE REQUEST ===");
            System.out.println("Employee ID: " + employeeId);
            
            // Load the PersonDeleteReq class
            Class<?> personDeleteReqClass = Class.forName(PERSON_DELETE_REQ_CLASS);
            Object personDeleteReq = personDeleteReqClass.getDeclaredConstructor().newInstance();
            
            // Get all methods for inspection
            Method[] methods = personDeleteReqClass.getDeclaredMethods();
            Map<String, Method> methodMap = new HashMap<>();
            
            System.out.println("DEBUG - Available PersonDeleteReq methods:");
            for (Method method : methods) {
                methodMap.put(method.getName().toLowerCase(), method);
                if (method.getName().startsWith("set")) {
                    System.out.println("  " + method.getName() + " -> " + method.getParameterTypes()[0].getSimpleName());
                }
            }
            
            // Set employee ID (sn field) - try multiple variations
            // First try passing the ID as a List since setSn expects List<String>
            List<String> employeeIdList = Arrays.asList(employeeId);
            boolean snSet = setMethodValue(personDeleteReq, methodMap, "setsn", employeeIdList, "setSn") ||
                           setMethodValue(personDeleteReq, methodMap, "setid", employeeIdList, "setId") ||
                           setMethodValue(personDeleteReq, methodMap, "setemployeeid", employeeIdList, "setEmployeeId") ||
                           setMethodValue(personDeleteReq, methodMap, "setpersonid", employeeIdList, "setPersonId") ||
                           // Also try with single string value  
                           setMethodValue(personDeleteReq, methodMap, "setsn", employeeId, "setSn (String)") ||
                           setMethodValue(personDeleteReq, methodMap, "setid", employeeId, "setId (String)") ||
                           setMethodValue(personDeleteReq, methodMap, "setemployeeid", employeeId, "setEmployeeId (String)") ||
                           setMethodValue(personDeleteReq, methodMap, "setpersonid", employeeId, "setPersonId (String)");
            
            if (!snSet) {
                System.err.println("WARNING: Could not set employee ID using any method!");
                // List all available setter methods
                for (Method method : methods) {
                    if (method.getName().toLowerCase().contains("set") && method.getParameterTypes().length == 1) {
                        System.out.println("Available setter: " + method.getName());
                    }
                }
            }
            
            System.out.println("=== PERSON DELETE REQUEST BUILT SUCCESSFULLY ===");
            return personDeleteReq;
            
        } catch (Exception e) {
            System.err.println("ERROR: Failed to build PersonDeleteReq: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to build person delete request: " + e.getMessage(), e);
        }
    }

    /**
     * Build PersonFindListReq object for getting list of persons
     */
    public Object buildPersonFindListReq() throws Exception {
        Class<?> personFindListReqClass = Class.forName("com.hfims.xcan.gateway.netty.client.req.PersonFindListReq");
        Object personFindListReq = personFindListReqClass.getDeclaredConstructor().newInstance();
        
        System.out.println("DEBUG - Created PersonFindListReq: " + personFindListReq.getClass().getName());
        
        // PersonFindListReq might have pagination parameters
        try {
            // Try to set page number (usually starts from 1)
            java.lang.reflect.Field pageField = personFindListReqClass.getDeclaredField("page");
            pageField.setAccessible(true);
            pageField.set(personFindListReq, 1);
            System.out.println("DEBUG - Set page to 1");
        } catch (NoSuchFieldException e) {
            System.out.println("DEBUG - No page field found in PersonFindListReq");
        }
        
        try {
            // Try to set page size (get all records)
            java.lang.reflect.Field sizeField = personFindListReqClass.getDeclaredField("size");
            sizeField.setAccessible(true);
            sizeField.set(personFindListReq, 100); // Get up to 100 records
            System.out.println("DEBUG - Set size to 100");
        } catch (NoSuchFieldException e) {
            System.out.println("DEBUG - No size field found in PersonFindListReq");
        }
        
        try {
            // Try to set pageNum (alternative to page)
            java.lang.reflect.Field pageNumField = personFindListReqClass.getDeclaredField("pageNum");
            pageNumField.setAccessible(true);
            pageNumField.set(personFindListReq, 1);
            System.out.println("DEBUG - Set pageNum to 1");
        } catch (NoSuchFieldException e) {
            System.out.println("DEBUG - No pageNum field found in PersonFindListReq");
        }
        
        try {
            // Try to set pageSize (alternative to size)
            java.lang.reflect.Field pageSizeField = personFindListReqClass.getDeclaredField("pageSize");
            pageSizeField.setAccessible(true);
            pageSizeField.set(personFindListReq, 100);
            System.out.println("DEBUG - Set pageSize to 100");
        } catch (NoSuchFieldException e) {
            System.out.println("DEBUG - No pageSize field found in PersonFindListReq");
        }
        
        return personFindListReq;
    }

    /**
     * Helper method to set values using reflection with type conversion
     */
    private boolean setMethodValue(Object target, Map<String, Method> methodMap, 
                                  String methodName, Object value, String displayName) {
        Method method = null;
        try {
            method = methodMap.get(methodName);
            if (method != null) {
                method.setAccessible(true);
                
                // Handle type conversion
                Class<?>[] paramTypes = method.getParameterTypes();
                if (paramTypes.length == 1) {
                    Object convertedValue = convertValue(value, paramTypes[0]);
                    method.invoke(target, convertedValue);
                    System.out.println("Successfully set " + displayName + " = " + convertedValue + 
                                     " (type: " + paramTypes[0].getSimpleName() + ")");
                    return true;
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to set " + displayName + ": " + e.getMessage());
            if (method != null && method.getParameterTypes().length > 0) {
                System.err.println("Expected type: " + method.getParameterTypes()[0].getSimpleName());
            }
        }
        return false;
    }
    
    /**
     * Convert value to the expected parameter type
     */
    private Object convertValue(Object value, Class<?> targetType) {
        if (value == null) return null;
        if (targetType.isAssignableFrom(value.getClass())) return value;
        
        String stringValue = value.toString();
        
        // Handle common type conversions
        if (targetType == int.class || targetType == Integer.class) {
            return Integer.parseInt(stringValue);
        } else if (targetType == long.class || targetType == Long.class) {
            return Long.parseLong(stringValue);
        } else if (targetType == double.class || targetType == Double.class) {
            return Double.parseDouble(stringValue);
        } else if (targetType == float.class || targetType == Float.class) {
            return Float.parseFloat(stringValue);
        } else if (targetType == boolean.class || targetType == Boolean.class) {
            return Boolean.parseBoolean(stringValue);
        } else if (targetType == byte.class || targetType == Byte.class) {
            return Byte.parseByte(stringValue);
        } else if (targetType == short.class || targetType == Short.class) {
            return Short.parseShort(stringValue);
        } else if (targetType == char.class || targetType == Character.class) {
            return stringValue.length() > 0 ? stringValue.charAt(0) : '\0';
        }
        
        return stringValue; // Default to string
    }

    /**
     * Get available methods for debugging
     */
    public void inspectPersonCreateReqMethods() {
        try {
            Class<?> clazz = Class.forName(PERSON_CREATE_REQ_CLASS);
            Method[] methods = clazz.getDeclaredMethods();
            
            System.out.println("=== PersonCreateReq Available Methods ===");
            for (Method method : methods) {
                if (method.getName().startsWith("set")) {
                    System.out.println("Method: " + method.getName() + 
                                     " | Parameter: " + method.getParameterTypes()[0].getSimpleName());
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to inspect methods: " + e.getMessage());
        }
    }
}
