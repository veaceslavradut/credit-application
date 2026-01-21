import apiClient from './apiClient';
import { Offer } from '@/types/api';

export const offerService = {
  async getForApplication(applicationId: string): Promise<Offer[]> {
    const response = await apiClient.get<Offer[]>(
      `/borrower/applications/${applicationId}/offers`
    );
    return response.data;
  },

  async acceptOffer(offerId: string): Promise<Offer> {
    const response = await apiClient.post<Offer>(`/borrower/offers/${offerId}/accept`);
    return response.data;
  },

  async rejectOffer(offerId: string): Promise<Offer> {
    const response = await apiClient.post<Offer>(`/borrower/offers/${offerId}/reject`);
    return response.data;
  },
};