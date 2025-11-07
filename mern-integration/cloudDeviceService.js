// Cloud Device Service for MERN App Integration (Non-Docker Deployment)
// services/cloudDeviceService.js

class CloudDeviceService {
  constructor(apiBaseUrl = null) {
    // Flexible API URL configuration for different deployment scenarios
    this.apiBaseUrl = apiBaseUrl || 
                     process.env.REACT_APP_API_URL || 
                     process.env.NEXT_PUBLIC_API_URL ||
                     'http://localhost:8081/api';
    
    // Remove trailing slash if present
    this.apiBaseUrl = this.apiBaseUrl.replace(/\/$/, '');
    
    // Default device credentials (should be configurable in your app)
    this.defaultDeviceKey = '020e7096a03c670f63';
    this.defaultSecret = '123456';
    
    // Request timeout (30 seconds for device operations)
    this.timeout = 30000;
    
    console.log(`🌐 CloudDeviceService initialized with API: ${this.apiBaseUrl}`);
  }

  // Helper method for making HTTP requests with timeout
  async makeRequest(endpoint, options = {}) {
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), this.timeout);
    
    try {
      const response = await fetch(`${this.apiBaseUrl}${endpoint}`, {
        ...options,
        signal: controller.signal,
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json',
          ...options.headers,
        },
      });
      
      clearTimeout(timeoutId);
      
      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(`HTTP ${response.status}: ${errorText}`);
      }
      
      const data = await response.json();
      console.log(`✅ API Request successful: ${endpoint}`, data);
      return data;
    } catch (error) {
      clearTimeout(timeoutId);
      console.error(`❌ API Request failed: ${endpoint}`, error);
      throw error;
    }
  }

  // Health check to verify cloud gateway is running
  async healthCheck() {
    try {
      // Try the actuator health endpoint first (Spring Boot standard)
      const response = await this.makeRequest('/actuator/health');
      return response;
    } catch (error) {
      // Fallback to custom health endpoint
      try {
        const fallbackResponse = await this.makeRequest('/health');
        return fallbackResponse;
      } catch (fallbackError) {
        console.error('All health check endpoints failed:', error, fallbackError);
        throw new Error('Gateway health check failed - service may be down');
      }
    }
  }

  // Get server information for debugging
  async getServerInfo() {
    try {
      const response = await this.makeRequest('/info');
      return response;
    } catch (error) {
      console.error('Server info failed:', error);
      throw error;
    }
  }

  // Test device connectivity
  async testDevice(deviceKey = this.defaultDeviceKey, secret = this.defaultSecret) {
    try {
      const response = await this.makeRequest('/device/test', {
        method: 'POST',
        body: JSON.stringify({ deviceKey, secret })
      });
      
      console.log('✅ Device test successful:', response);
      return response;
    } catch (error) {
      console.error('❌ Device test failed:', error);
      throw new Error(`Device test failed: ${error.message}`);
    }
  }

  // Get device information
  async getDeviceInfo(deviceKey = this.defaultDeviceKey, secret = this.defaultSecret) {
    try {
      const response = await this.makeRequest('/device/info', {
        method: 'POST',
        body: JSON.stringify({ deviceKey, secret })
      });
      
      console.log('✅ Device info retrieved:', response);
      return response;
    } catch (error) {
      console.error('❌ Get device info failed:', error);
      throw new Error(`Get device info failed: ${error.message}`);
    }
  }

  // Get device status for dashboard
  async getDeviceStatus(deviceKey = this.defaultDeviceKey, secret = this.defaultSecret) {
    try {
      const response = await this.makeRequest(`/status?deviceKey=${encodeURIComponent(deviceKey)}&secret=${encodeURIComponent(secret)}`, {
        method: 'GET'
      });
      
      console.log('✅ Device status retrieved:', response);
      return result;
    } catch (error) {
      console.error('Get device status failed:', error);
      throw error;
    }
  }

  // Reboot device
  async rebootDevice(deviceKey = this.defaultDeviceKey, secret = this.defaultSecret) {
    try {
      const response = await fetch(`${this.apiBaseUrl}/reboot`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ deviceKey, secret })
      });
      
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      
      const result = await response.json();
      return response;
    } catch (error) {
      console.error('❌ Device status failed:', error);
      throw new Error(`Device status failed: ${error.message}`);
    }
  }

  // Configure API base URL (for dynamic cloud server switching)
  setApiUrl(newUrl) {
    this.apiBaseUrl = newUrl.replace(/\/$/, ''); // Remove trailing slash
    console.log(`🔄 API URL updated to: ${this.apiBaseUrl}`);
  }

  // Get current API URL
  getApiUrl() {
    return this.apiBaseUrl;
  }

  // Validate connection to gateway
  async validateConnection() {
    try {
      const health = await this.healthCheck();
      const info = await this.getServerInfo();
      
      return {
        success: true,
        health,
        info,
        message: 'Gateway connection validated successfully'
      };
    } catch (error) {
      return {
        success: false,
        error: error.message,
        message: 'Gateway connection validation failed'
      };
    }
  }

  // Batch operations for multiple devices
  async batchOperation(operation, devices) {
    console.log(`🔄 Starting batch operation: ${operation} for ${devices.length} devices`);
    const results = [];
    
    for (const device of devices) {
      try {
        let result;
        switch (operation) {
          case 'test':
            result = await this.testDevice(device.deviceKey, device.secret);
            break;
          case 'status':
            result = await this.getDeviceStatus(device.deviceKey, device.secret);
            break;
          case 'info':
            result = await this.getDeviceInfo(device.deviceKey, device.secret);
            break;
          default:
            throw new Error(`Unknown operation: ${operation}`);
        }
        
        results.push({
          deviceKey: device.deviceKey,
          success: true,
          data: result
        });
      } catch (error) {
        results.push({
          deviceKey: device.deviceKey,
          success: false,
          error: error.message
        });
      }
    }
    
    console.log(`✅ Batch operation completed: ${results.filter(r => r.success).length}/${results.length} successful`);
    return results;
  }
}

// Export for different module systems
// ES6 modules
export default CloudDeviceService;
export { CloudDeviceService };

// CommonJS (Node.js)
if (typeof module !== 'undefined' && module.exports) {
  module.exports = CloudDeviceService;
  module.exports.CloudDeviceService = CloudDeviceService;
}

// Usage examples and deployment configurations:
/*
=== LOCAL DEVELOPMENT ===
const deviceService = new CloudDeviceService('http://localhost:8081/api');

=== CLOUD DEPLOYMENT EXAMPLES ===

// Heroku
const deviceService = new CloudDeviceService('https://your-app.herokuapp.com/api');

// Railway  
const deviceService = new CloudDeviceService('https://your-app.railway.app/api');

// Render
const deviceService = new CloudDeviceService('https://your-app.onrender.com/api');

// Custom Domain
const deviceService = new CloudDeviceService('https://api.yourdomain.com/api');

=== REACT USAGE ===
import CloudDeviceService from './services/cloudDeviceService.js';

function DeviceDashboard() {
  const [deviceService] = useState(() => new CloudDeviceService(
    process.env.REACT_APP_API_URL || 'http://localhost:8081/api'
  ));

  useEffect(() => {
    async function checkGateway() {
      try {
        const validation = await deviceService.validateConnection();
        if (validation.success) {
          console.log('✅ Gateway connected:', validation.info);
        }
      } catch (error) {
        console.error('❌ Gateway connection failed:', error);
      }
    }
    checkGateway();
  }, [deviceService]);
}

=== NEXT.JS USAGE ===
import CloudDeviceService from '../services/cloudDeviceService.js';

export async function getServerSideProps() {
  const deviceService = new CloudDeviceService(process.env.NEXT_PUBLIC_API_URL);
  const health = await deviceService.healthCheck();
  
  return {
    props: { health }
  };
}

=== ENVIRONMENT VARIABLES ===
# React (.env)
REACT_APP_API_URL=https://your-gateway.herokuapp.com/api

# Next.js (.env.local)  
NEXT_PUBLIC_API_URL=https://your-gateway.railway.app/api

# Node.js Backend (.env)
API_URL=https://your-gateway.onrender.com/api

=== API ENDPOINTS (After Deployment) ===
Health Check: GET /actuator/health
Device Test:  POST /api/device/test
Device Info:  POST /api/device/info  
App Status:   GET /api/status
App Info:     GET /api/info
*/