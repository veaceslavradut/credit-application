# 5. API INTEGRATION LAYER

### 5.1 Axios Setup with Interceptors

**apiClient.ts** - Centralized HTTP client:
```typescript
import axios, { AxiosInstance, AxiosError } from 'axios';
import { useAuthStore } from '@/stores/authStore';
import { useUIStore } from '@/stores/uiStore';

// Create Axios instance with base config
const apiClient: AxiosInstance = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor: Add JWT token to all requests
apiClient.interceptors.request.use((config) => {
  const token = useAuthStore.getState().token;
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
}, (error) => Promise.reject(error));

// Response interceptor: Handle errors & refresh tokens
apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as any;

    // Handle 401 Unauthorized - try to refresh token
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        const { refreshToken } = useAuthStore.getState();
        const response = await axios.post(
          `${process.env.NEXT_PUBLIC_API_URL}/auth/refresh`,
          { refreshToken }
        );

        const { access_token, refresh_token } = response.data;
        useAuthStore.getState().setTokens(access_token, refresh_token);

        // Retry original request with new token
        originalRequest.headers.Authorization = `Bearer ${access_token}`;
        return apiClient(originalRequest);
      } catch (refreshError) {
        // Refresh failed - logout user
        useAuthStore.getState().clearAuth();
        window.location.href = '/auth/login';
        return Promise.reject(refreshError);
      }
    }

    // Handle 403 Forbidden - access denied
    if (error.response?.status === 403) {
      useUIStore.getState().addToast({
        type: 'error',
        message: 'Access denied. You do not have permission to perform this action.',
      });
    }

    // Handle 5xx server errors
    if (error.response?.status && error.response.status >= 500) {
      useUIStore.getState().addToast({
        type: 'error',
        message: 'Server error. Please try again later.',
      });
    }

    return Promise.reject(error);
  }
);

export default apiClient;
```

### 5.2 API Service Pattern

**applicationService.ts** - Encapsulate API calls by domain:
```typescript
import apiClient from './apiClient';
import { CreateApplicationDTO, UpdateApplicationDTO, Application } from '@/types/api.types';

export const applicationService = {
  // Create new application
  async create(data: CreateApplicationDTO): Promise<{ data: Application }> {
    const response = await apiClient.post<Application>('/applications', data);
    return { data: response.data };
  },

  // Fetch single application
  async get(id: string): Promise<{ data: Application }> {
    const response = await apiClient.get<Application>(`/applications/${id}`);
    return { data: response.data };
  },

  // List applications for borrower
  async list(params?: { status?: string; page?: number; limit?: number }) {
    const response = await apiClient.get<{ data: Application[]; total: number }>('/applications', {
      params,
    });
    return response.data;
  },

  // Update application (draft only)
  async update(id: string, data: UpdateApplicationDTO): Promise<{ data: Application }> {
    const response = await apiClient.put<Application>(`/applications/${id}`, data);
    return { data: response.data };
  },

  // Submit application with consent
  async submit(id: string, consentGiven: boolean): Promise<{ data: Application }> {
    const response = await apiClient.post<Application>(`/applications/${id}/submit`, {
      consentGiven,
    });
    return { data: response.data };
  },

  // Get application status
  async getStatus(id: string): Promise<{ data: { status: string; updatedAt: string } }> {
    const response = await apiClient.get(`/applications/${id}/status`);
    return { data: response.data };
  },

  // Get status history
  async getHistory(id: string): Promise<{ data: { status: string; timestamp: string }[] }> {
    const response = await apiClient.get(`/applications/${id}/history`);
    return { data: response.data };
  },
};
```

### 5.3 Error Handling Pattern

**errorHandler.ts** - Centralized error management:
```typescript
import { AxiosError } from 'axios';

export interface ApiErrorResponse {
  code: string;
  message: string;
  details?: Record<string, string[]>;
  timestamp: string;
}

export class ApiError extends Error {
  constructor(
    public statusCode: number,
    public code: string,
    message: string,
    public details?: Record<string, string[]>
  ) {
    super(message);
    this.name = 'ApiError';
  }
}

export function handleApiError(error: unknown): ApiError {
  // Handle Axios errors
  if (error instanceof AxiosError) {
    const data = error.response?.data as ApiErrorResponse;
    return new ApiError(
      error.response?.status || 500,
      data?.code || 'UNKNOWN_ERROR',
      data?.message || error.message,
      data?.details
    );
  }

  // Handle other errors
  if (error instanceof Error) {
    return new ApiError(500, 'UNKNOWN_ERROR', error.message);
  }

  return new ApiError(500, 'UNKNOWN_ERROR', 'An unexpected error occurred');
}

export function getUserFriendlyMessage(error: ApiError): string {
  const messages: Record<string, string> = {
    VALIDATION_ERROR: 'Please check your input and try again',
    UNAUTHORIZED: 'You are not authorized to perform this action',
    NOT_FOUND: 'The requested resource was not found',
    CONFLICT: 'This action conflicts with existing data',
    RATE_LIMITED: 'Too many requests. Please wait and try again',
  };

  return messages[error.code] || error.message || 'An error occurred. Please try again.';
}
```

---
