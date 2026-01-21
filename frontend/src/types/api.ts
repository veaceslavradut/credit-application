export interface User {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  role: 'BORROWER' | 'BANK_ADMIN' | 'COMPLIANCE_OFFICER';
  organizationId?: string;
  isActive: boolean;
  createdAt: string;
}

export interface AuthResponse {
  token: string;
  refreshToken?: string;
  user: User;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface Application {
  id: string;
  borrowerId: string;
  loanType: string;
  loanAmount: number;
  loanTermMonths: number;
  currency: string;
  status: string;
  createdAt: string;
}

export interface Offer {
  id: string;
  applicationId: string;
  bankName: string;
  apr: number;
  monthlyPayment: number;
  expiresAt: string;
}

export interface ApiResponse<T> {
  data: T;
  message?: string;
}