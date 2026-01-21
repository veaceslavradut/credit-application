import apiClient from './apiClient';
import { User, AuthResponse, LoginRequest } from '@/types/api';

export const authService = {
  async login(email: string, password: string): Promise<AuthResponse> {
    const { data } = await apiClient.post<AuthResponse>('/auth/login', {
      email,
      password,
    });
    if (data.token) {
      typeof window !== 'undefined' && localStorage.setItem('authToken', data.token);
    }
    return data;
  },

  async logout(): Promise<void> {
    typeof window !== 'undefined' && localStorage.removeItem('authToken');
  },

  async getProfile(): Promise<User> {
    const { data } = await apiClient.get<User>('/auth/profile');
    return data;
  },

  async refreshToken(): Promise<string> {
    const { data } = await apiClient.post<{ token: string }>('/auth/refresh');
    return data.token;
  },
};