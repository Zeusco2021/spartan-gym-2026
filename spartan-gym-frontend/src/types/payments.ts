export interface Subscription {
  id: string;
  userId: string;
  planType: string;
  status: 'active' | 'suspended' | 'cancelled' | 'expired';
  paymentProvider: 'stripe' | 'adyen';
  startedAt: string;
  expiresAt?: string;
}

export interface Transaction {
  id: string;
  userId: string;
  amount: number;
  currency: string;
  type: 'subscription' | 'donation' | 'refund';
  status: 'completed' | 'pending' | 'failed' | 'refunded';
  createdAt: string;
}

export interface Donation {
  id: string;
  donorId: string;
  creatorId: string;
  amount: number;
  currency: string;
  message?: string;
  createdAt: string;
}

export interface PaymentMethod {
  id: string;
  userId: string;
  provider: 'stripe' | 'adyen' | 'paypal';
  type: 'card' | 'bank_account' | 'paypal';
  last4?: string;
  expiryMonth?: number;
  expiryYear?: number;
  isDefault: boolean;
}
