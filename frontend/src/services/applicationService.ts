import apiClient from './apiClient';
import { Application, CreateApplicationRequest, ApiResponse } from '@/types/api';

export const applicationService = {
  async create(data: CreateApplicationRequest): Promise<Application> {
    const response = await apiClient.post<Application>('/borrower/applications', data);
    return response.data;
  },

  async getById(id: string): Promise<Application> {
    const response = await apiClient.get<Application>(`/borrower/applications/${id}`);
    return response.data;
  },

  async list(page: number = 0, size: number = 10): Promise<any> {
    const response = await apiClient.get<any>('/borrower/applications', {
      params: { page, size },
    });
    return response.data;
  },

  async update(id: string, data: Partial<Application>): Promise<Application> {
    const response = await apiClient.put<Application>(`/borrower/applications/${id}`, data);
    return response.data;
  },

  async submit(id: string): Promise<Application> {
    const response = await apiClient.post<Application>(`/borrower/applications/${id}/submit`);
    return response.data;
  },

  async getHistory(id: string): Promise<any> {
    const response = await apiClient.get<any>(`/borrower/applications/${id}/history`);
    return response.data;
  },
};