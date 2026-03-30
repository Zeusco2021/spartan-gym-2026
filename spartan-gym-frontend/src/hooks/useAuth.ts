import { useAppDispatch, useAppSelector } from '@/app/hooks';
import { setCredentials, setMfaPending, logout } from '@/features/auth/authSlice';
import { usersApi } from '@/api/usersApi';
import { baseApi } from '@/api/baseApi';
import { socketService } from '@/websocket/socketService';
import type { LoginRequest } from '@/types';

export function useAuth() {
  const { user, isAuthenticated, token } = useAppSelector((state) => state.auth);
  const dispatch = useAppDispatch();
  const [loginMutation] = usersApi.useLoginMutation();
  const [registerMutation] = usersApi.useRegisterMutation();

  const login = async (credentials: LoginRequest) => {
    const result = await loginMutation(credentials).unwrap();
    if (result.mfaRequired) {
      // When MFA is required, the backend returns a session token for MFA verification.
      // The current LoginResponse type doesn't include sessionToken, so we cast to access it.
      const sessionToken = (result as unknown as { sessionToken: string }).sessionToken ?? '';
      dispatch(setMfaPending(sessionToken));
      return { mfaRequired: true as const };
    }
    dispatch(setCredentials(result));
    socketService.connect(result.token, dispatch);
    return { mfaRequired: false as const };
  };

  const handleLogout = () => {
    socketService.disconnect();
    dispatch(logout());
    dispatch(baseApi.util.resetApiState());
  };

  return {
    user,
    isAuthenticated,
    token,
    login,
    register: registerMutation,
    logout: handleLogout,
  };
}
