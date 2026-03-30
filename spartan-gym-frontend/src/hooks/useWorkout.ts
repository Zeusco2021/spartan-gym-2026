import { useState } from 'react';
import { workoutsApi } from '@/api/workoutsApi';
import type { WorkoutSession, WorkoutSet } from '@/types';

export function useWorkout() {
  const [startMutation] = workoutsApi.useStartWorkoutMutation();
  const [addSetMutation] = workoutsApi.useAddSetMutation();
  const [completeMutation] = workoutsApi.useCompleteWorkoutMutation();
  const [activeSession, setActiveSession] = useState<WorkoutSession | null>(null);

  const startWorkout = async (planId?: string, routineId?: string) => {
    const session = await startMutation({ planId, routineId }).unwrap();
    setActiveSession(session);
    return session;
  };

  const addSet = async (set: Omit<WorkoutSet, 'id' | 'timestamp'>) => {
    if (!activeSession) throw new Error('No hay sesión activa');
    return addSetMutation({ sessionId: activeSession.id, set }).unwrap();
  };

  const completeWorkout = async () => {
    if (!activeSession) throw new Error('No hay sesión activa');
    const result = await completeMutation(activeSession.id).unwrap();
    setActiveSession(null);
    return result;
  };

  return { activeSession, startWorkout, addSet, completeWorkout };
}
