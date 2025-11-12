package com.hfims.xcan.gateway.tcp.demo.service;

import com.hfims.xcan.gateway.netty.client.HfDeviceClient;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Service to inspect available methods in HfDeviceClient
 */
@Service
public class DeviceMethodInspector {

    public void inspectDeviceClientMethods() {
        try {
            Class<?> clazz = HfDeviceClient.class;
            Method[] methods = clazz.getDeclaredMethods();
            
            System.out.println("=== HfDeviceClient Available Methods ===");
            Arrays.stream(methods)
                .filter(method -> java.lang.reflect.Modifier.isPublic(method.getModifiers()))
                .filter(method -> java.lang.reflect.Modifier.isStatic(method.getModifiers()))
                .sorted((a, b) -> a.getName().compareTo(b.getName()))
                .forEach(method -> {
                    System.out.println("Method: " + method.getName());
                    System.out.println("  Parameters: " + Arrays.toString(method.getParameterTypes()));
                    System.out.println("  Return Type: " + method.getReturnType().getSimpleName());
                    System.out.println("  ---");
                });
                
        } catch (Exception e) {
            System.err.println("Error inspecting methods: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Map<String, Object> inspectDeviceClient() {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> methods = new ArrayList<>();
        
        try {
            Class<?> clazz = HfDeviceClient.class;
            Method[] allMethods = clazz.getDeclaredMethods();
            
            Arrays.stream(allMethods)
                .filter(method -> java.lang.reflect.Modifier.isPublic(method.getModifiers()))
                .filter(method -> java.lang.reflect.Modifier.isStatic(method.getModifiers()))
                .sorted((a, b) -> a.getName().compareTo(b.getName()))
                .forEach(method -> {
                    Map<String, Object> methodInfo = new HashMap<>();
                    methodInfo.put("name", method.getName());
                    methodInfo.put("parameters", Arrays.toString(method.getParameterTypes()));
                    methodInfo.put("returnType", method.getReturnType().getSimpleName());
                    methods.add(methodInfo);
                });
            
            result.put("className", clazz.getSimpleName());
            result.put("packageName", clazz.getPackage().getName());
            result.put("totalMethods", methods.size());
            result.put("methods", methods);
            result.put("timestamp", new Date());
                
        } catch (Exception e) {
            result.put("error", e.getMessage());
            result.put("methods", new ArrayList<>());
        }
        
        return result;
    }
}