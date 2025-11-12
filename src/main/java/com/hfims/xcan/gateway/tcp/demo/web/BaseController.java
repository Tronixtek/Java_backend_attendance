package com.hfims.xcan.gateway.tcp.demo.web;

import com.hfims.xcan.gateway.netty.client.dto.HostInfoDto;
import com.hfims.xcan.gateway.netty.error.CgiErrorEnum;
import com.hfims.xcan.gateway.netty.error.CgiErrorException;
import com.hfims.xcan.gateway.tcp.demo.support.IpUtils;
import com.hfims.xcan.gateway.netty.util.StringUtils;
import com.hfims.xcan.gateway.tcp.demo.config.HfGatewayDemoConfig;

public abstract class BaseController {

    // Use environment variable for device IP, fallback to Digital Ocean server IP
    private static final String DEVICE_IP = System.getenv("DEVICE_IP") != null ? 
        System.getenv("DEVICE_IP") : "143.198.150.26";
    
    final HostInfoDto hostInfo = new HostInfoDto(DEVICE_IP, HfGatewayDemoConfig.SDK_PORT, HfGatewayDemoConfig.TIMEOUT);

    // Constructor to log host info for debugging
    public BaseController() {
        System.out.println("DEBUG - Gateway Host Info: " + DEVICE_IP + ":" + HfGatewayDemoConfig.SDK_PORT + " (timeout: " + HfGatewayDemoConfig.TIMEOUT + "ms)");
        System.out.println("DEBUG - Environment: DEVICE_IP=" + System.getenv("DEVICE_IP") + " (using: " + DEVICE_IP + ")");
    }

    protected void validateCommon(String deviceKey, String secret)
            throws CgiErrorException {
        if (StringUtils.isBlank(deviceKey) || deviceKey.length() < 16) {
            throw new CgiErrorException(CgiErrorEnum.CODE_1000);
        }
        if (StringUtils.isBlank(secret)) {
            throw new CgiErrorException(CgiErrorEnum.CODE_1000);
        }
    }
}
