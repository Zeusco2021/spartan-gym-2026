import { useEffect } from 'react';
import { useAppSelector } from '@/app/hooks';
import { socketService } from '@/websocket/socketService';

export function useGymOccupancy(gymId: string) {
  const occupancy = useAppSelector(
    (state) => state.websocket.occupancyUpdates[gymId],
  );

  useEffect(() => {
    socketService.joinGymOccupancy(gymId);
    return () => {
      socketService.leaveGymOccupancy(gymId);
    };
  }, [gymId]);

  return occupancy;
}
