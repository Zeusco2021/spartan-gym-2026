import { baseApi } from './baseApi';
import type { Gym, GymEquipment } from '@/types';

export const gymsApi = baseApi.injectEndpoints({
  endpoints: (builder) => ({
    getNearbyGyms: builder.query<
      Gym[],
      { lat: number; lng: number; radius?: number }
    >({
      query: (params) => ({ url: '/api/gyms/nearby', params }),
      providesTags: ['Gym'],
    }),
    getGymById: builder.query<Gym, string>({
      query: (id) => `/api/gyms/${id}`,
      providesTags: (_, __, id) => [{ type: 'Gym', id }],
    }),
    getGymEquipment: builder.query<GymEquipment[], string>({
      query: (gymId) => `/api/gyms/${gymId}/equipment`,
    }),
    getGymOccupancy: builder.query<
      { current: number; max: number },
      string
    >({
      query: (gymId) => `/api/gyms/${gymId}/occupancy`,
    }),
    checkin: builder.mutation<
      { success: boolean },
      { gymId: string; qrCode: string }
    >({
      query: ({ gymId, ...body }) => ({
        url: `/api/gyms/${gymId}/checkin`,
        method: 'POST',
        body,
      }),
    }),
  }),
});

export const {
  useGetNearbyGymsQuery,
  useGetGymByIdQuery,
  useGetGymEquipmentQuery,
  useGetGymOccupancyQuery,
  useCheckinMutation,
} = gymsApi;
