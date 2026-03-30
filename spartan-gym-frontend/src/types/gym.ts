export interface Gym {
  id: string;
  chainId?: string;
  name: string;
  address: string;
  latitude: number;
  longitude: number;
  operatingHours: Record<string, { open: string; close: string }>;
  maxCapacity: number;
  currentOccupancy?: number;
}

export interface GymEquipment {
  id: string;
  gymId: string;
  name: string;
  category: string;
  quantity: number;
  status: 'available' | 'maintenance' | 'out_of_order';
}
